import json
import os
import uuid
from datetime import datetime, timezone
from decimal import Decimal

import boto3
from botocore.exceptions import ClientError

DYNAMODB = boto3.resource("dynamodb")
USERS = DYNAMODB.Table(os.environ["USERS_TABLE"])
RIDES = DYNAMODB.Table(os.environ["RIDES_TABLE"])
BOOKINGS = DYNAMODB.Table(os.environ["BOOKINGS_TABLE"])


def _json_default(value):
    if isinstance(value, Decimal):
        return int(value) if value % 1 == 0 else float(value)
    raise TypeError(f"Unsupported value: {type(value)!r}")


def response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {"content-type": "application/json"},
        "body": json.dumps(body, default=_json_default),
    }


def body(event):
    raw = event.get("body") or "{}"
    return json.loads(raw) if isinstance(raw, str) else raw


def user_id(event):
    return (
        event.get("requestContext", {})
        .get("authorizer", {})
        .get("jwt", {})
        .get("claims", {})
        .get("sub")
    )


def now():
    return datetime.now(timezone.utc).isoformat()


def get_rides(event):
    params = event.get("queryStringParameters") or {}
    result = RIDES.scan()
    items = [item for item in result.get("Items", []) if item.get("availableSeats", 0) > 0]
    if params.get("from"):
        items = [item for item in items if params["from"].lower() in item.get("from", "").lower()]
    if params.get("to"):
        items = [item for item in items if params["to"].lower() in item.get("to", "").lower()]
    if params.get("date"):
        items = [item for item in items if item.get("date") == params["date"]]
    return response(200, {"rides": items})


def get_ride(event):
    ride_id = event.get("pathParameters", {}).get("rideId")
    item = RIDES.get_item(Key={"rideId": ride_id}).get("Item")
    return response(200, item) if item else response(404, {"message": "Ride not found"})


def create_ride(event):
    driver = user_id(event)
    payload = body(event)
    required = ["from", "to", "date", "departure", "distanceKm", "farePerKm", "passengerSeats"]
    if not driver or any(field not in payload for field in required):
        return response(400, {"message": "Missing authenticated user or required ride fields"})
    if float(payload["farePerKm"]) < 3:
        return response(400, {"message": "Fare per kilometre must be at least ₹3"})
    seats = int(payload["passengerSeats"])
    if seats < 1 or seats > 4:
        return response(400, {"message": "Passenger seats must be between 1 and 4"})

    ride_id = str(uuid.uuid4())
    item = {
        "rideId": ride_id,
        "driverId": driver,
        "from": payload["from"],
        "to": payload["to"],
        "date": payload["date"],
        "routeDate": payload["date"],
        "departure": payload["departure"],
        "distanceKm": Decimal(str(payload["distanceKm"])),
        "farePerKm": Decimal(str(payload["farePerKm"])),
        "passengerSeats": seats,
        "availableSeats": seats,
        "status": "OPEN",
        "createdAt": now(),
    }
    RIDES.put_item(Item=item)
    return response(201, item)


def get_profile(event):
    item = USERS.get_item(Key={"userId": user_id(event)}).get("Item")
    return response(200, item or {"userId": user_id(event)})


def update_profile(event):
    payload = body(event)
    item = {"userId": user_id(event), **payload, "updatedAt": now()}
    USERS.put_item(Item=item)
    return response(200, item)


def create_booking(event):
    passenger = user_id(event)
    ride_id = event.get("pathParameters", {}).get("rideId")
    seats = int(body(event).get("seats", 1))
    if not passenger or not ride_id or seats < 1:
        return response(400, {"message": "A ride and a positive seat count are required"})

    ride = RIDES.get_item(Key={"rideId": ride_id}).get("Item")
    if not ride:
        return response(404, {"message": "Ride not found"})
    booking_id = str(uuid.uuid4())
    booking = {
        "bookingId": booking_id,
        "passengerId": passenger,
        "rideId": ride_id,
        "seats": seats,
        "status": "CONFIRMED",
        "estimatedFare": Decimal(str(float(ride["distanceKm"]) * float(ride["farePerKm"]) * seats)),
        "createdAt": now(),
    }
    try:
        DYNAMODB.meta.client.transact_write_items(
            TransactItems=[
                {
                    "Update": {
                        "TableName": RIDES.name,
                        "Key": {"rideId": ride_id},
                        "UpdateExpression": "SET availableSeats = availableSeats - :seats",
                        "ConditionExpression": "availableSeats >= :seats AND #status = :open",
                        "ExpressionAttributeNames": {"#status": "status"},
                        "ExpressionAttributeValues": {":seats": seats, ":open": "OPEN"},
                    }
                },
                {"Put": {"TableName": BOOKINGS.name, "Item": booking}},
            ]
        )
    except ClientError as error:
        if error.response["Error"]["Code"] == "TransactionCanceledException":
            return response(409, {"message": "Not enough seats available"})
        raise
    return response(201, booking)


def list_bookings(event):
    result = BOOKINGS.query(
        IndexName="passenger-index",
        KeyConditionExpression="passengerId = :passenger",
        ExpressionAttributeValues={":passenger": user_id(event)},
    )
    return response(200, {"bookings": result.get("Items", [])})


def delete_booking(event):
    booking_id = event.get("pathParameters", {}).get("bookingId")
    booking = BOOKINGS.get_item(Key={"bookingId": booking_id}).get("Item")
    if not booking or booking.get("passengerId") != user_id(event):
        return response(404, {"message": "Booking not found"})
    DYNAMODB.meta.client.transact_write_items(
        TransactItems=[
            {
                "Update": {
                    "TableName": RIDES.name,
                    "Key": {"rideId": booking["rideId"]},
                    "UpdateExpression": "SET availableSeats = availableSeats + :seats",
                    "ExpressionAttributeValues": {":seats": booking["seats"]},
                }
            },
            {"Delete": {"TableName": BOOKINGS.name, "Key": {"bookingId": booking_id}}},
        ]
    )
    return {"statusCode": 204, "body": ""}


def lambda_handler(event, context):
    method = event.get("requestContext", {}).get("http", {}).get("method", "")
    path = event.get("rawPath", "")
    if method == "GET" and path == "/rides":
        return get_rides(event)
    if method == "GET" and path.startswith("/rides/"):
        return get_ride(event)
    if method == "POST" and path == "/rides":
        return create_ride(event)
    if method == "POST" and "/bookings" in path and "/rides/" in path:
        return create_booking(event)
    if method == "GET" and path == "/bookings":
        return list_bookings(event)
    if method == "DELETE" and path.startswith("/bookings/"):
        return delete_booking(event)
    if method == "GET" and path == "/profile":
        return get_profile(event)
    if method == "PUT" and path == "/profile":
        return update_profile(event)
    return response(404, {"message": "Route not found"})
