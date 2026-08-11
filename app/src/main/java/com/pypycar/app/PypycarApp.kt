package com.pypycar.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal = Color(0xFF0F766E)

@Composable
fun PypycarApp() {
    var tab by remember { mutableIntStateOf(0) }
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    var showOffer by remember { mutableStateOf(false) }
    var posted by remember { mutableStateOf(false) }
    if (showOffer) {
        OfferRideScreen(onBack = { showOffer = false }, onPublished = { posted = true; showOffer = false })
        return
    }
    if (selectedRide != null) {
        RideDetails(selectedRide!!, onBack = { selectedRide = null })
        return
    }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("Find a ride", "My bookings", "Profile").forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(if (index == 0) Icons.Default.Home else if (index == 1) Icons.Default.CalendarMonth else Icons.Default.Person, label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(Modifier.padding(padding), onRide = { selectedRide = it }, onPost = { showOffer = true })
            1 -> BookingsScreen(Modifier.padding(padding))
            else -> ProfileScreen(Modifier.padding(padding), posted)
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, onRide: (Ride) -> Unit, onPost: () -> Unit) {
    var from by remember { mutableStateOf("Pune") }
    var to by remember { mutableStateOf("Mumbai") }
    var searching by remember { mutableStateOf(false) }
    val rides = if (searching) SampleData.rides.filter { it.from.contains(from, true) && it.to.contains(to, true) } else SampleData.rides
    Column(modifier.fillMaxSize().background(Color(0xFFF7FAF9))) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("pypycar", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
            Text("Share the journey. Share the joy.", color = Color(0xFF60716D))
            Spacer(Modifier.height(20.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Where are you going?", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    OutlinedTextField(from, { from = it }, Modifier.fillMaxWidth(), label = { Text("Leaving from") }, singleLine = true)
                    OutlinedTextField(to, { to = it }, Modifier.fillMaxWidth(), label = { Text("Going to") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = {}, Modifier.weight(1f)) { Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp)); Text("  Sat, 17 Aug") }
                        Button(onClick = { searching = true }, Modifier.weight(1f)) { Icon(Icons.Default.Search, null); Text("  Search") }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (searching) "Rides from $from to $to" else "Popular rides", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TextButton(onClick = onPost) { Icon(Icons.Default.Add, null); Text("Offer a ride") }
        }
        LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(rides) { RideCard(it, onRide) }
        }
    }
}

@Composable
private fun RideCard(ride: Ride, onRide: (Ride) -> Unit) {
    Card(onClick = { onRide(ride) }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("${ride.departure}  •  ${ride.date}", color = Teal, fontWeight = FontWeight.Bold); Text("${ride.from}  →  ${ride.to}", fontSize = 19.sp, fontWeight = FontWeight.SemiBold) }
                Column(horizontalAlignment = Alignment.End) { Text("₹${ride.fareFor(1).toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("per seat", color = Color(0xFF60716D), fontSize = 12.sp) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(34.dp).background(Color(0xFFD9F0EC), RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) { Text(ride.driver.name.take(1), color = Teal, fontWeight = FontWeight.Bold) }
                Text(ride.driver.name, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.Verified, null, tint = Teal, Modifier.size(17.dp))
                Text("★ ${ride.driver.rating}", color = Color(0xFFB77900))
                Spacer(Modifier.weight(1f))
                Text("${ride.availableSeats} seats left", color = Teal, fontWeight = FontWeight.Medium)
            }
            Text("${ride.distanceKm.toInt()} km  •  ${ride.driver.vehicle?.make} ${ride.driver.vehicle?.model}", color = Color(0xFF60716D), fontSize = 13.sp)
        }
    }
}

@Composable
private fun RideDetails(ride: Ride, onBack: () -> Unit) {
    var seats by remember { mutableIntStateOf(1) }
    var booked by remember { mutableStateOf(false) }
    Scaffold(topBar = { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Ride details", fontSize = 20.sp, fontWeight = FontWeight.Bold) } }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("${ride.from}  →  ${ride.to}", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Teal)
            Text("${ride.date}  •  ${ride.departure}", fontSize = 16.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F5F2))) { Column(Modifier.padding(16.dp)) { Text("Estimated fare", color = Color(0xFF60716D)); Text("₹${ride.fareFor(seats).toInt()}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Teal); Text("${ride.distanceKm.toInt()} km × ₹${ride.farePerKm.toInt()} × $seats seat(s)") } }
            Text("Driver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${ride.driver.name}  •  ★ ${ride.driver.rating} (${ride.driver.rides} rides)")
            Text("${ride.driver.vehicle?.make} ${ride.driver.vehicle?.model}  •  ${ride.driver.vehicle?.registration}", color = Color(0xFF60716D))
            Text("Seats to book", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) { OutlinedButton(onClick = { if (seats > 1) seats-- }) { Text("−") }; Text("  $seats  ", fontSize = 20.sp); OutlinedButton(onClick = { if (seats < ride.availableSeats) seats++ }) { Text("+") } }
            if (booked) Text("Booking confirmed. Coordinate payment directly with the driver via UPI.", color = Teal, fontWeight = FontWeight.SemiBold)
            else Button(onClick = { booked = true }, Modifier.fillMaxWidth(), enabled = ride.availableSeats >= seats) { Text("Reserve $seats seat${if (seats > 1) "s" else ""}") }
        }
    }
}

@Composable
private fun BookingsScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("My bookings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Pune  →  Mumbai", fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("Sat, 17 Aug  •  07:30 AM"); Text("1 seat  •  ₹600", color = Teal, fontWeight = FontWeight.SemiBold); Text("Confirmed", color = Color(0xFF18794E)) } }
    }
}

@Composable
private fun OfferRideScreen(onBack: () -> Unit, onPublished: () -> Unit) {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var fare by remember { mutableStateOf("4") }
    var seats by remember { mutableStateOf("3") }
    Scaffold(topBar = { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Offer a ride", fontSize = 20.sp, fontWeight = FontWeight.Bold) } }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Help someone get there", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Teal); Text("Payments are agreed directly through UPI.", color = Color(0xFF60716D)) }
            item { OutlinedTextField(from, { from = it }, Modifier.fillMaxWidth(), label = { Text("Starting location") }, singleLine = true) }
            item { OutlinedTextField(to, { to = it }, Modifier.fillMaxWidth(), label = { Text("Destination") }, singleLine = true) }
            item { OutlinedButton(onClick = {}, Modifier.fillMaxWidth()) { Icon(Icons.Default.CalendarMonth, null); Text("  Sat, 17 Aug  •  07:30 AM") } }
            item { OutlinedTextField(distance, { distance = it }, Modifier.fillMaxWidth(), label = { Text("Distance (km)") }, singleLine = true) }
            item { OutlinedTextField(fare, { fare = it }, Modifier.fillMaxWidth(), label = { Text("Fare per km (₹3 minimum)") }, singleLine = true) }
            item { OutlinedTextField(seats, { seats = it }, Modifier.fillMaxWidth(), label = { Text("Passenger seats") }, singleLine = true) }
            item { Text("Your vehicle supports up to 4 passenger seats. The driver's seat is not counted.", color = Color(0xFF60716D), fontSize = 13.sp) }
            item { Button(onClick = onPublished, Modifier.fillMaxWidth(), enabled = from.isNotBlank() && to.isNotBlank() && distance.toDoubleOrNull() != null && (fare.toDoubleOrNull() ?: 0.0) >= 3 && (seats.toIntOrNull() ?: 0) in 1..4) { Text("Publish ride") } }
        }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier, posted: Boolean) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(56.dp).background(Color(0xFFD9F0EC), RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) { Text("A", fontSize = 24.sp, color = Teal, fontWeight = FontWeight.Bold) }; Column(Modifier.padding(start = 14.dp)) { Text("Aarav Mehta", fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("★ 4.9  •  24 rides", color = Color(0xFF60716D)) } } }
        Text("Driver mode", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (posted) Text("Your ride is published and visible to passengers.", color = Teal)
        OutlinedButton(onClick = {}) { Icon(Icons.Default.DirectionsCar, null); Text("  Add vehicle details") }
    }
}
