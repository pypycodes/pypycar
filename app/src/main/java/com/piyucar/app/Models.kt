package com.piyucar.app

import java.time.LocalDate

data class Vehicle(val make: String, val model: String, val registration: String, val passengerCapacity: Int)
data class User(val id: String, val name: String, val rating: Double, val rides: Int, val vehicle: Vehicle? = null)
data class Ride(
    val id: String,
    val driver: User,
    val from: String,
    val to: String,
    val date: String,
    val departure: String,
    val distanceKm: Double,
    val farePerKm: Double,
    val passengerSeats: Int,
    val bookedSeats: Int = 0,
    val notes: String = ""
) {
    val availableSeats get() = passengerSeats - bookedSeats
    fun fareFor(seats: Int) = distanceKm * farePerKm * seats
}

data class Booking(val id: String, val passengerId: String, val ride: Ride, val seats: Int, val status: String = "Confirmed")

object SampleData {
    val currentUser = User("me", "Aarav Mehta", 4.9, 24)
    val rides = listOf(
        Ride("1", User("r1", "Rohan Sharma", 4.8, 42, Vehicle("Maruti", "Ertiga", "MH 12 AB 1234", 6)), "Pune", "Mumbai", "Sat, 17 Aug", "07:30 AM", 150.0, 4.0, 4, 2),
        Ride("2", User("r2", "Ananya Iyer", 5.0, 18, Vehicle("Hyundai", "Creta", "MH 14 CD 5678", 5)), "Pune", "Mumbai", "Sat, 17 Aug", "09:00 AM", 148.0, 3.5, 3, 1),
        Ride("3", User("r3", "Vikram Patel", 4.7, 67, Vehicle("Tata", "Nexon", "MH 01 EF 9012", 5)), "Pune", "Nashik", "Sun, 18 Aug", "06:45 AM", 210.0, 3.0, 3)
    )
}
