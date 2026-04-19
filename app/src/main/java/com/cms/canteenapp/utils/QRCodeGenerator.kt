package com.cms.canteenapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRCodeGenerator @Inject constructor() {

    fun generateBookingQR(bookingId: String): String {
        // Generate QR code data with booking info
        return "CANTEEN:$bookingId:${System.currentTimeMillis()}"
    }

    fun generateQRCodeBitmap(data: String, width: Int = 512, height: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}