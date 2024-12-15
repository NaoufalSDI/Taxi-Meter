package com.example.taxixact

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var ageField: TextInputEditText
    private lateinit var permitField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var confirmPasswordField: TextInputEditText
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingVideo: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
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

        // Initialiser Firestore
        db = Firebase.firestore

        nameField = findViewById(R.id.nomComplet)
        emailField = findViewById(R.id.emailEt)
        ageField = findViewById(R.id.age)
        permitField = findViewById(R.id.permit)
        passwordField = findViewById(R.id.passET)
        confirmPasswordField = findViewById(R.id.confirmEt)
        loadingVideo = findViewById(R.id.loading_video)

        val alreadyRegisteredTextView: TextView = findViewById(R.id.sign_up_to_have_account_login)
        alreadyRegisteredTextView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.button).setOnClickListener {
            signUp()
        }
    }

    private fun makeSystemBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    private fun signUp() {
        val name = nameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val ageText = ageField.text.toString().trim()
        val permit = permitField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val confirmPassword = confirmPasswordField.text.toString().trim()

        // Validations for user input
        if (name.isEmpty()) {
            nameField.error = "Le nom est obligatoire"
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Entrez un email valide"
            return
        }

        val age = ageText.toIntOrNull()
        if (age == null || age < 18 || age > 100) {
            ageField.error = "L'âge doit être compris entre 18 et 100"
            return
        }

        if (permit.isEmpty()) {
            permitField.error = "Le type de permis est obligatoire"
            return
        }

        if (password.isEmpty() || password.length < 8) {
            passwordField.error = "Le mot de passe doit contenir au moins 8 caractères"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordField.error = "Les mots de passe ne correspondent pas"
            return
        }

        // Show loading video animation
        val videoUri = Uri.parse("android.resource://${packageName}/raw/loading_hand_animation")
        loadingVideo.setVideoURI(videoUri)
        loadingVideo.visibility = View.VISIBLE
        loadingVideo.start()

        // Start Firebase Authentication process
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User created successfully, save additional user data in Firestore
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    firebaseUser?.let { user ->
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "age" to age,
                            "permit" to permit
                        )

                        // Store user data in Firestore using UID
                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Hide the video on success
                                loadingVideo.visibility = View.GONE

                                // Show success message and redirect to SignIn
                                Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                loadingVideo.visibility = View.GONE
                                Toast.makeText(this, "Erreur Firestore : ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Hide video and show error message for Firebase Auth registration failure
                    loadingVideo.visibility = View.GONE
                    Toast.makeText(this, "Erreur lors de l'inscription : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                loadingVideo.visibility = View.GONE
            }
    }

    // Function to check if email exists in Firestore
    private fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Callback indicating whether the email exists
                if (querySnapshot.isEmpty) {
                    callback(false)
                } else {
                    callback(true)
                }
            }
            .addOnFailureListener { e ->
                // Handle error while checking email
                Toast.makeText(this, "Erreur lors de la vérification de l'email : ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)  // Treat it as if the email doesn't exist on error
            }
    }

}