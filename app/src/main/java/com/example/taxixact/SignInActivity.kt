package com.example.taxixact

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isSigningIn = false
    private lateinit var signInLoading: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val layout: ConstraintLayout = findViewById(R.id.main)
        val currentLocale = resources.configuration.locale
        if (currentLocale.language == "ar") {
            layout.setBackgroundResource(R.drawable.sign_screen_back_ar)
        } else {
            layout.setBackgroundResource(R.drawable.sign_screen_back)
        }
        makeSystemBarsTransparent()

        emailField = findViewById(R.id.emailEt)
        passwordField = findViewById(R.id.passET)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        signInLoading = findViewById(R.id.signInLoading)

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.signInButton).setOnClickListener {
            signIn()
        }

        findViewById<TextView>(R.id.signUpTextView).setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
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

    private fun signIn() {
        if (isSigningIn) return

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        // Validate email and password
        if (email.isEmpty()) {
            emailField.error = "L'email est obligatoire"
            return
        }

        if (password.isEmpty() || password.length < 8) {
            passwordField.error = "Le mot de passe doit contenir au moins 8 caractères"
            return
        }

        isSigningIn = true

        signInLoading.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                signInLoading.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                signInLoading.visibility = View.GONE
                isSigningIn = false
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "incorrect Email or Password"
                    is FirebaseAuthInvalidUserException -> "Utilisateur introuvable ou désactivé"
                    else -> "Une erreur inconnue est survenue : ${exception.message}"
                }
                CustomToast.show(this, "Erreur : $errorMessage", Color.parseColor("#F5FFFFFF"), R.drawable.error_icon, R.drawable.error_background)

            }
    }
}
