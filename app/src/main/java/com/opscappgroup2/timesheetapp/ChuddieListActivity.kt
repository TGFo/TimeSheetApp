package com.opscappgroup2.timesheetapp

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChuddieListActivity : AppCompatActivity() {

    private lateinit var chuddieGrid: GridLayout
    private lateinit var userId: String
    private lateinit var buttonBackToNavigation: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chuddie_list)


        userId = FirebaseAuth.getInstance().uid ?: return
        chuddieGrid = findViewById(R.id.chuddieGrid)
        buttonBackToNavigation = findViewById(R.id.buttonBackToNavigation)

        initializeChuddieCounts()

        buttonBackToNavigation.setOnClickListener {
            finish()
        }
    }

    private fun initializeChuddieCounts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chuddiesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddies")

        chuddiesRef.get().addOnSuccessListener { snapshot ->
            // Check if the "chuddies" node already exists
            if (!snapshot.exists()) {
                // Initialize Chuddie counts to zero
                val chuddieData = mapOf(
                    "chudJack" to 0,
                    "midLifer" to 0,
                    "serenity" to 0,
                    "gigaChud" to 0,
                    "femcel" to 0,
                    "soylessOne" to 0
                )

                // Set the initial counts in the database
                chuddiesRef.setValue(chuddieData).addOnSuccessListener {
                    Toast.makeText(this, "Chuddie counts initialized successfully!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to initialize Chuddie counts: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                // Retrieve Chuddie counts from the database
                loadChuddieCounts()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error checking Chuddie counts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadChuddieCounts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chuddiesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddies")

        chuddiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Get the counts for each Chuddie
                val chudJackCount = snapshot.child("chudJack").getValue(Int::class.java) ?: 0
                val midLiferCount = snapshot.child("midLifer").getValue(Int::class.java) ?: 0
                val serenityCount = snapshot.child("serenity").getValue(Int::class.java) ?: 0
                val gigaChudCount = snapshot.child("gigaChud").getValue(Int::class.java) ?: 0
                val femcelCount = snapshot.child("femcel").getValue(Int::class.java) ?: 0
                val soylessOneCount = snapshot.child("soylessOne").getValue(Int::class.java) ?: 0

                // Update the TextViews with the counts
                findViewById<TextView>(R.id.chuddieText1).text = "$chudJackCount ChudJack"
                findViewById<TextView>(R.id.chuddieText2).text = "$midLiferCount MidLifer"
                findViewById<TextView>(R.id.chuddieText3).text = "$serenityCount Serenity"
                findViewById<TextView>(R.id.chuddieText4).text = "$gigaChudCount GigaChud"
                findViewById<TextView>(R.id.chuddieText5).text = "$femcelCount Femcel"
                findViewById<TextView>(R.id.chuddieText6).text = "$soylessOneCount SoylessOne"
            } else {
                // Handle case where the "chuddies" node does not exist (e.g., new user)
                Toast.makeText(this, "No Chuddie counts found. Please initialize.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load Chuddie counts: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}