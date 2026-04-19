package com.cms.canteenapp.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourapp.canteen.data.models.MealType
import com.yourapp.canteen.navigation.Screen
import com.yourapp.canteen.ui.components.LoadingDialog
import com.yourapp.canteen.ui.viewmodels.AuthViewModel
import com.yourapp.canteen.ui.viewmodels.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSeatScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser
    val availableSeats by bookingViewModel.availableSeats
    val selectedSeat by bookingViewModel.selectedSeat
    val selectedMealType by bookingViewModel.selectedMealType
    val isLoading by bookingViewModel.isLoading
    val bookingSuccess by bookingViewModel.bookingSuccess
    val error by bookingViewModel.error

    var showConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bookingViewModel.loadAvailableSeats()
    }

    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess) {
            bookingViewModel.clearBookingSuccess()
            navController.navigate(Screen.MyBookings.route) {
                popUpTo(Screen.UserDashboard.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book a Seat") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Meal Type Selection
            Text(
                text = "Select Meal Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealType.values().forEach { mealType ->
                    FilterChip(
                        selected = selectedMealType == mealType,
                        onClick = { bookingViewModel.selectMealType(mealType) },
                        label = { Text(mealType.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seat Selection
            Text(
                text = "Select a Seat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${availableSeats.size} seats available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seat Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableSeats) { seat ->
                    SeatItem(
                        seatNumber = seat.seatNumber,
                        isSelected = selectedSeat == seat.seatNumber,
                        onClick = { bookingViewModel.selectSeat(seat.seatNumber) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Booking Summary
            if (selectedSeat != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Booking Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seat: $selectedSeat",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Meal: ${selectedMealType.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error Message
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Book Button
            Button(
                onClick = { showConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSeat != null && !isLoading
            ) {
                Text("Book Seat")
            }
        }
    }

    if (showConfirmation && selectedSeat != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Booking") },
            text = {
                Text("Do you want to book Seat $selectedSeat for ${selectedMealType.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        currentUser?.let {
                            bookingViewModel.createBooking(
                                userId = it.uid,
                                userName = it.name.ifEmpty { "User" },
                                userPhone = it.phoneNumber
                            )
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isLoading) {
        LoadingDialog()
    }
}

@Composable
fun SeatItem(
    seatNumber: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = seatNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}