package com.cms.canteenapp.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yourapp.canteen.navigation.Screen
import com.yourapp.canteen.ui.viewmodels.AuthState
import com.yourapp.canteen.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var showCountryCodeDialog by remember { mutableStateOf(false) }
    var countryCode by remember { mutableStateOf("+91") }

    val authState by viewModel.authState
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                val user = (authState as AuthState.Authenticated).user
                if (user.isAdmin) {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.UserDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is AuthState.CodeSent -> {
                navController.navigate(Screen.OtpVerification.route.replace(
                    "{phoneNumber}",
                    "$countryCode$phoneNumber"
                ))
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Canteen Booking",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Enter your phone number to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 10) phoneNumber = it },
                label = { Text("Phone Number") },
                leadingIcon = {
                    TextButton(onClick = { showCountryCodeDialog = true }) {
                        Text(text = countryCode)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (phoneNumber.length == 10) {
                        viewModel.sendVerificationCode(
                            "$countryCode$phoneNumber",
                            context as Activity
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.length == 10 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Send OTP")
            }
        }
    }

    if (showCountryCodeDialog) {
        CountryCodePickerDialog(
            onDismiss = { showCountryCodeDialog = false },
            onSelect = { code ->
                countryCode = code
                showCountryCodeDialog = false
            }
        )
    }
}

@Composable
fun CountryCodePickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val countryCodes = listOf(
        "+91" to "India",
        "+1" to "USA",
        "+44" to "UK",
        "+61" to "Australia",
        "+971" to "UAE"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Country Code") },
        text = {
            Column {
                countryCodes.forEach { (code, country) ->
                    TextButton(
                        onClick = { onSelect(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$country ($code)")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}