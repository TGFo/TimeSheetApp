package com.opscappgroup2.timesheetapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfilePageActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var businessNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var buttonSaveProfile: Button
    private lateinit var buttonBackToNavigation: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilepage)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        usernameEditText = findViewById(R.id.usernameEditText)
        businessNameEditText = findViewById(R.id.businessNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)
        buttonBackToNavigation = findViewById(R.id.buttonBackToNavigation)

        loadUserProfile()

        buttonSaveProfile.setOnClickListener {
            saveUserProfile()
        }

        buttonBackToNavigation.setOnClickListener {
            navigateToNavigationActivity()
        }
    }

    private fun loadUserProfile() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").getValue(String::class.java)
            val businessName = snapshot.child("businessName").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)

            usernameEditText.setText(username)
            businessNameEditText.setText(businessName)
            emailEditText.setText(email)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfile() {
        val username = usernameEditText.text.toString().trim()
        val businessName = businessNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        val updates = mapOf(
            "username" to username,
            "businessName" to businessName,
            "email" to email
        )

        // Update email in Firebase Authentication
        auth.currentUser?.updateEmail(email)?.addOnSuccessListener {
            // Update user profile in the Realtime Database
            userRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update email in Firebase Authentication: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToNavigationActivity() {
        val intent = Intent(this, NavigationActivity::class.java)
        startActivity(intent)
        finish()
    }
}