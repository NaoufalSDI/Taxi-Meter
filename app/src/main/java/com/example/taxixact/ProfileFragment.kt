package com.example.taxixact

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var helloTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var qrCodeImageView: ImageView
    private lateinit var loadingVideoView: ProgressBar
    private lateinit var profileInformationLayout: View
    private lateinit var backProfileTextView: TextView

    private var onBackClickListener: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        helloTextView = view.findViewById(R.id.helloTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView)
        loadingVideoView = view.findViewById(R.id.loadingVideoView)
        profileInformationLayout = view.findViewById(R.id.profile_informations)
        backProfileTextView = view.findViewById(R.id.back_profile)


        val signOutButton: AppCompatButton = view.findViewById(R.id.sign_out_profile)

        backProfileTextView.setOnClickListener {
            onBackClickListener?.invoke()
            parentFragmentManager.popBackStack()
        }

        signOutButton.setOnClickListener {
            signOutAndNavigateToLogin()
        }

        fetchUserData()
    }

    // Fetch user data from Firebase
    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")?.uppercase() ?: "User"
                        val email = document.getString("email") ?: "email@example.com"
                        val age = document.getLong("age")?.toInt() ?: 0
                        val permit = document.getString("permit")?.uppercase() ?: "N/A"

                        helloTextView.text = "Hello, $name"
                        emailTextView.text = email

                        val qrData = "Name: $name\nEmail: $email\nAge: $age\nPermit: $permit"
                        generateQRCode(qrData)

                        slideInProfileInfo()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun slideInProfileInfo() {
        profileInformationLayout.visibility = View.VISIBLE

        val slideIn = ObjectAnimator.ofFloat(profileInformationLayout, "translationY", 500f, 0f)
        slideIn.duration = 500
        slideIn.interpolator = DecelerateInterpolator()
        slideIn.start()
    }

    private fun generateQRCode(data: String) {
        try {
            val width = 500
            val height = 500

            val bitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height)

            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) {
                        bitmap.setPixel(x, y, Color.parseColor("#FF9E00"))
                    } else {
                        bitmap.setPixel(x, y, Color.parseColor("#FFFFFF"))
                    }
                }
            }

            qrCodeImageView.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to generate QR code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setOnBackClickListener(listener: (() -> Unit)?) {
        onBackClickListener = listener
    }

    private fun signOutAndNavigateToLogin() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackClickListener?.invoke()
    }
}
