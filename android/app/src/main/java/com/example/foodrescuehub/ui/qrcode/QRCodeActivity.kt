package com.example.foodrescuehub.ui.qrcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.api.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * QR Code Activity - Display pickup QR code for order
 */
class QRCodeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_STORE_NAME = "store_name"
        const val EXTRA_PICKUP_START = "pickup_start"
        const val EXTRA_PICKUP_END = "pickup_end"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivQrCode: ImageView
    private lateinit var tvQrCodeText: TextView
    private lateinit var tvStoreName: TextView
    private lateinit var tvPickupTime: TextView

    private var orderId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        // Get extras
        orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0)
        val storeName = intent.getStringExtra(EXTRA_STORE_NAME) ?: "Store"
        val pickupStart = intent.getStringExtra(EXTRA_PICKUP_START)
        val pickupEnd = intent.getStringExtra(EXTRA_PICKUP_END)

        initViews()
        setupToolbar()
        displayOrderInfo(storeName, pickupStart, pickupEnd)
        loadQRCode()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivQrCode = findViewById(R.id.ivQrCode)
        tvQrCodeText = findViewById(R.id.tvQrCodeText)
        tvStoreName = findViewById(R.id.tvStoreName)
        tvPickupTime = findViewById(R.id.tvPickupTime)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayOrderInfo(storeName: String, pickupStart: String?, pickupEnd: String?) {
        tvStoreName.text = storeName

        // Format pickup time
        val pickupTime = if (pickupStart != null && pickupEnd != null) {
            val start = formatTime(pickupStart)
            val end = formatTime(pickupEnd)
            "Today $start-$end"
        } else {
            "Pickup time not specified"
        }
        tvPickupTime.text = pickupTime
    }

    private fun loadQRCode() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("QRCodeActivity", "Loading QR code for order $orderId")
                val response = RetrofitClient.apiService.generatePickupQRCode(orderId)

                android.util.Log.d("QRCodeActivity", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val qrData = response.body()
                    android.util.Log.d("QRCodeActivity", "QR data: $qrData")

                    qrData?.let {
                        val qrHash = it["qrTokenHash"] ?: ""
                        val qrCodeUrl = it["qrCodeUrl"] ?: ""

                        android.util.Log.d("QRCodeActivity", "QR Hash: $qrHash")

                        // Display QR code hash
                        tvQrCodeText.text = "Code: ${qrHash.take(8)}"

                        // Load QR code image
                        // For now, generate QR code locally from the hash
                        android.util.Log.d("QRCodeActivity", "Generating QR code bitmap...")
                        generateQRCodeBitmap(qrHash)?.let { bitmap ->
                            android.util.Log.d("QRCodeActivity", "QR code bitmap generated successfully")
                            ivQrCode.setImageBitmap(bitmap)
                        } ?: run {
                            android.util.Log.e("QRCodeActivity", "Failed to generate QR code bitmap")
                            Toast.makeText(
                                this@QRCodeActivity,
                                "Failed to generate QR code image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    android.util.Log.e("QRCodeActivity", "API call failed: ${response.code()}")
                    Toast.makeText(
                        this@QRCodeActivity,
                        "Failed to load QR code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("QRCodeActivity", "Error loading QR code", e)
                Toast.makeText(
                    this@QRCodeActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateQRCodeBitmap(content: String): Bitmap? {
        return try {
            android.util.Log.d("QRCodeActivity", "Generating bitmap for content: $content")

            // Use ZXing library to generate QR code
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            android.util.Log.d("QRCodeActivity", "Bitmap created: ${bitmap.width}x${bitmap.height}")
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("QRCodeActivity", "Error generating QR code bitmap", e)
            null
        }
    }

    private fun formatTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
