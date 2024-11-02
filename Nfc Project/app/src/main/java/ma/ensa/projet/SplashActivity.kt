package ma.ensa.projet

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ma.ensa.projet.splash.NFCArcsView
import ma.ensa.projet.splash.NFCTextAnimator

// SplashActivity.kt
class SplashActivity : AppCompatActivity() {
    private lateinit var nfcTextView: TextView
    private lateinit var nfcArcsView: NFCArcsView
    private lateinit var nfcTextAnimator: NFCTextAnimator // Declare NFCTextAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "onCreate: Starting")

        nfcTextView = findViewById(R.id.nfcTextView)
        nfcArcsView = findViewById(R.id.nfcArcsView)

        // Initialize the NFCTextAnimator
        nfcTextAnimator = NFCTextAnimator(nfcTextView)

        // Start animations
        startNFCTextAnimation()
        startNFCArcsAnimation()

        // Navigate to main activity after animations
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SplashActivity", "Navigating to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 seconds total duration
    }

    private fun startNFCTextAnimation() {
        nfcTextAnimator.start() // Start NFC text animation
    }

    private fun startNFCArcsAnimation() {
        nfcArcsView.startAnimation()
    }
}

// NFCArcsView.kt remains unchanged
