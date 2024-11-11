package com.opscappgroup2.timesheetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth and Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val businessNameEditText: EditText = findViewById(R.id.businessNameEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val backToLoginButton: Button = findViewById(R.id.backToLoginButton)

        // Navigate back to the login screen
        backToLoginButton.setOnClickListener {
            navigateToLogin()
        }

        // Handle user registration
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val businessName = businessNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (!validateInput(username, businessName, email, password)) return@setOnClickListener

            registerUser(username, businessName, email, password)
        }
    }

    // Validate input fields
    private fun validateInput(username: String, businessName: String, email: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                showError("Username is required")
                false
            }
            businessName.isEmpty() -> {
                showError("Business name is required")
                false
            }
            email.isEmpty() -> {
                showError("Email is required")
                false
            }
            password.isEmpty() -> {
                showError("Password is required")
                false
            }
            else -> true
        }
    }

    // Register the user with Firebase Authentication
    private fun registerUser(username: String, businessName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                user?.let {
                    saveUserToDatabase(it, username, businessName, email)
                }
            }
            .addOnFailureListener { e ->
                showError("Registration failed: ${e.message}")
            }
    }

    // Save user information to Firebase Realtime Database
    private fun saveUserToDatabase(user: FirebaseUser, username: String, businessName: String, email: String) {
        val userId = user.uid
        val userMap = mapOf(
            "username" to username,
            "businessName" to businessName,
            "email" to email
        )

        // Save user data under "Users/userId"
        val userRef = database.reference.child("Users").child(userId)
        userRef.setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .addOnFailureListener { e ->
                showError("Failed to save user info: ${e.message}")
            }
    }

    // Navigate to the login screen
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Display an error message
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}