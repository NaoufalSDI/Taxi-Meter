package com.example.taxixact

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), MapFragment.MapFragmentListener {

    private lateinit var mapFragment: MapFragment
    private lateinit var toggleButton: Button
    private lateinit var distanceTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var fareTextView: TextView
    private lateinit var profileImageView: ImageView

    private var isTracking = false
    private var totalDistance = 0f
    private var totalTime = 0L
    private var handler: Handler? = null
    private val updateInterval = 1000L

    // Pricing rates
    private val baseFare = 2.5
    private val ratePerKilometer = 0.015
    private val ratePerMinute = 0.5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        makeSystemBarsTransparent()

        toggleButton = findViewById(R.id.toggleButton)
        distanceTextView = findViewById(R.id.distance)
        timerTextView = findViewById(R.id.timer)
        fareTextView = findViewById(R.id.price)
        profileImageView = findViewById(R.id.profile)

        loadMapFragment()

        profileImageView.setOnClickListener {
            showProfileFragment()
        }

        toggleButton.setOnClickListener {
            toggleTracking()
        }

        toggleButton.setText(getString(R.string.toggle_button_text_start))
    }

    private fun loadMapFragment() {
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .commit()
    }

    private fun toggleTracking() {
        if (!EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            CustomToast.show(
                this,
                "Location permission is required to track your ride.",
                Color.parseColor("#F5FFFFFF"),
                R.drawable.error_icon,
                R.drawable.error_background
            )
            mapFragment.requestLocationPermission()
            return
        }

        isTracking = !isTracking

        if (isTracking) {
            toggleButton.setText(getString(R.string.toggle_button_text_finish))
            mapFragment.startTracking()
            startRide()
        } else {
            toggleButton.setText(getString(R.string.toggle_button_text_start))
            mapFragment.stopTracking()
            endRide()
        }
    }

    private fun startRide() {
        totalDistance = 0f
        totalTime = 0L
        updateUI(0f, 0L, calculateFare(0f, 0L))

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(object : Runnable {
            override fun run() {
                if (isTracking) {
                    totalTime++
                    updateUI(totalDistance, totalTime, calculateFare(totalDistance, totalTime))
                    handler?.postDelayed(this, updateInterval)
                }
            }
        }, updateInterval)
    }

    private fun endRide() {
        handler?.removeCallbacksAndMessages(null)

        val fare = calculateFare(totalDistance, totalTime)
        sendNotification(totalDistance, totalTime, fare)

        distanceTextView.text = "0 m"
        timerTextView.text = "0 min"
        fareTextView.text = "0 DH"
    }

    override fun onDistanceUpdated(distance: Float) {
        if (isTracking) {
            totalDistance = distance
            updateUI(totalDistance, totalTime, calculateFare(totalDistance, totalTime))
        }
    }

    private fun updateUI(distance: Float, time: Long, fare: Double) {
        distanceTextView.text = String.format("%.2f m", distance)
        timerTextView.text = String.format("%d min", (time / 60))
        fareTextView.text = String.format("%.2f DH", fare)
    }

    private fun calculateFare(distance: Float, time: Long): Double {
        val distanceInKm = distance / 1000
        val timeInMinutes = time / 60
        return baseFare + (distanceInKm * ratePerKilometer) + (timeInMinutes * ratePerMinute)
    }

    private fun sendNotification(distance: Float, time: Long, fare: Double) {
        val distanceInKm = distance / 1000
        val timeInMinutes = time / 60

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ride_summary",
                "Ride Summary",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "ride_summary")
            .setSmallIcon(R.drawable.taxi_logo_splash)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                getString(R.string.notification_content, distanceInKm, timeInMinutes, fare)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

    }

    private fun showProfileFragment() {
        findViewById<LinearLayout>(R.id.header).visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        findViewById<LinearLayout>(R.id.payment_detail).visibility = View.GONE
        findViewById<Button>(R.id.toggleButton).visibility = View.GONE

        val profileFragment = ProfileFragment()
        profileFragment.setOnBackClickListener {
            findViewById<LinearLayout>(R.id.header).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.payment_detail).visibility = View.VISIBLE
            findViewById<Button>(R.id.toggleButton).visibility = View.VISIBLE
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.profile_fragment_container, profileFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager

        val profileFragment =
            fragmentManager.findFragmentById(R.id.profile_fragment_container) as? ProfileFragment

        if (profileFragment != null && profileFragment.isVisible) {
            findViewById<LinearLayout>(R.id.header).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.payment_detail).visibility = View.VISIBLE
            findViewById<Button>(R.id.toggleButton).visibility = View.VISIBLE

            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun makeSystemBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}