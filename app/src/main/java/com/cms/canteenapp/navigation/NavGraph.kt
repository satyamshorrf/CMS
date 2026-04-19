package com.cms.canteenapp.navigation



import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourapp.canteen.ui.screens.admin.AdminDashboardScreen
import com.yourapp.canteen.ui.screens.admin.ReportsScreen
import com.yourapp.canteen.ui.screens.admin.ScanQRScreen
import com.yourapp.canteen.ui.screens.admin.StockManagementScreen
import com.yourapp.canteen.ui.screens.admin.UserManagementScreen
import com.yourapp.canteen.ui.screens.auth.LoginScreen
import com.yourapp.canteen.ui.screens.auth.OtpVerificationScreen
import com.yourapp.canteen.ui.screens.user.BookSeatScreen
import com.yourapp.canteen.ui.screens.user.BookingDetailsScreen
import com.yourapp.canteen.ui.screens.user.MyBookingsScreen
import com.yourapp.canteen.ui.screens.user.UserDashboardScreen
import com.yourapp.canteen.ui.viewmodels.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val startDestination = remember {
        mutableStateOf(Screen.Login.route)
    }

    // Check auth state
    val currentUser by authViewModel.currentUser
    startDestination.value = if (currentUser != null) {
        if (currentUser?.isAdmin == true) Screen.AdminDashboard.route else Screen.UserDashboard.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination.value
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpVerificationScreen(
                navController = navController,
                phoneNumber = phoneNumber
            )
        }

        // User Screens
        composable(Screen.UserDashboard.route) {
            UserDashboardScreen(navController = navController)
        }

        composable(Screen.BookSeat.route) {
            BookSeatScreen(navController = navController)
        }

        composable(Screen.MyBookings.route) {
            MyBookingsScreen(navController = navController)
        }

        composable(
            route = Screen.BookingDetails.route,
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailsScreen(
                navController = navController,
                bookingId = bookingId
            )
        }

        // Admin Screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(navController = navController)
        }

        composable(Screen.StockManagement.route) {
            StockManagementScreen(navController = navController)
        }

        composable(Screen.ScanQR.route) {
            ScanQRScreen(navController = navController)
        }

        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
    }
}