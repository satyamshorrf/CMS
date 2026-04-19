package com.cms.canteenapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.canteen.data.models.Booking
import com.yourapp.canteen.data.models.MealType
import com.yourapp.canteen.data.models.Seat
import com.yourapp.canteen.data.repository.BookingRepository
import com.yourapp.canteen.utils.QRCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val qrCodeGenerator: QRCodeGenerator
) : ViewModel() {

    private val _availableSeats = MutableStateFlow<List<Seat>>(emptyList())
    val availableSeats: StateFlow<List<Seat>> = _availableSeats.asStateFlow()

    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    private val _selectedSeat = MutableStateFlow<String?>(null)
    val selectedSeat: StateFlow<String?> = _selectedSeat.asStateFlow()

    private val _selectedMealType = MutableStateFlow(MealType.LUNCH)
    val selectedMealType: StateFlow<MealType> = _selectedMealType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _bookingSuccess = MutableStateFlow(false)
    val bookingSuccess: StateFlow<Boolean> = _bookingSuccess.asStateFlow()

    fun loadAvailableSeats() {
        viewModelScope.launch {
            bookingRepository.getAvailableSeats().collect { seats ->
                _availableSeats.value = seats
            }
        }
    }

    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            bookingRepository.getUserBookings(userId).collect { bookings ->
                _userBookings.value = bookings
            }
        }
    }

    fun selectSeat(seatNumber: String) {
        _selectedSeat.value = seatNumber
    }

    fun selectMealType(mealType: MealType) {
        _selectedMealType.value = mealType
    }

    fun createBooking(userId: String, userName: String, userPhone: String) {
        viewModelScope.launch {
            val seatNumber = _selectedSeat.value
            if (seatNumber == null) {
                _error.value = "Please select a seat"
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            _bookingSuccess.value = false

            try {
                val booking = Booking(
                    userId = userId,
                    userName = userName,
                    userPhone = userPhone,
                    seatNumber = seatNumber,
                    mealType = _selectedMealType.value
                )

                // Generate QR code
                val bookingWithQR = booking.copy(
                    qrCode = qrCodeGenerator.generateBookingQR(booking.bookingId)
                )

                val result = bookingRepository.createBooking(bookingWithQR)
                result.onSuccess {
                    _bookingSuccess.value = true
                    _selectedSeat.value = null
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getBookingDetails(bookingId: String): Booking? {
        return bookingRepository.getBookingById(bookingId)
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = bookingRepository.cancelBooking(bookingId)
            result.onSuccess {
                // Refresh bookings
                _userBookings.value.forEach { booking ->
                    if (booking.bookingId == bookingId) {
                        loadUserBookings(booking.userId)
                    }
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }

            _isLoading.value = false
        }
    }

    fun clearBookingSuccess() {
        _bookingSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}