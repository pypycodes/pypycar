package com.pypycar.app

class RideRepository(initialRides: List<Ride> = SampleData.rides) {
    private val rides = initialRides.toMutableList()
    private val bookings = mutableListOf<Booking>()

    fun search(from: String, to: String, date: String? = null): List<Ride> = rides.filter {
        it.availableSeats > 0 && it.from.contains(from, ignoreCase = true) && it.to.contains(to, ignoreCase = true) && (date == null || it.date == date)
    }

    @Synchronized
    fun book(rideId: String, passenger: User, seats: Int): Result<Booking> {
        if (seats < 1) return Result.failure(IllegalArgumentException("At least one seat is required"))
        val index = rides.indexOfFirst { it.id == rideId }
        if (index < 0) return Result.failure(NoSuchElementException("Ride not found"))
        val ride = rides[index]
        if (seats > ride.availableSeats) return Result.failure(IllegalStateException("Not enough seats available"))
        val updated = ride.copy(bookedSeats = ride.bookedSeats + seats)
        rides[index] = updated
        return Booking("booking-${bookings.size + 1}", passenger.id, updated, seats).also { bookings += it }.let { Result.success(it) }
    }

    fun bookingsFor(passengerId: String): List<Booking> = bookings.filter { it.passengerId == passengerId }
}
