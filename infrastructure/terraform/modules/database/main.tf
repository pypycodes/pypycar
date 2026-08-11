resource "aws_dynamodb_table" "users" {
  name         = "${var.name_prefix}-users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"

  attribute {
    name = "userId"
    type = "S"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled = true
  }
}

resource "aws_dynamodb_table" "rides" {
  name         = "${var.name_prefix}-rides"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "rideId"

  attribute {
    name = "rideId"
    type = "S"
  }

  attribute {
    name = "routeDate"
    type = "S"
  }

  attribute {
    name = "driverId"
    type = "S"
  }

  global_secondary_index {
    name            = "route-date-index"
    hash_key        = "routeDate"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "driver-date-index"
    hash_key        = "driverId"
    projection_type = "ALL"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled = true
  }
}

resource "aws_dynamodb_table" "bookings" {
  name         = "${var.name_prefix}-bookings"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "bookingId"

  attribute {
    name = "bookingId"
    type = "S"
  }

  attribute {
    name = "passengerId"
    type = "S"
  }

  attribute {
    name = "rideId"
    type = "S"
  }

  global_secondary_index {
    name            = "passenger-index"
    hash_key        = "passengerId"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "ride-index"
    hash_key        = "rideId"
    projection_type = "ALL"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled = true
  }
}
