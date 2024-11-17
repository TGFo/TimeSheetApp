package com.opscappgroup2.timesheetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GachaActivity : AppCompatActivity() {

    private lateinit var pointsTextView: TextView
    private lateinit var buttonOneSummon: Button
    private lateinit var buttonBack: Button
    private lateinit var userId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var gachaImage: ImageView
    private var points = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gacha)

        pointsTextView = findViewById(R.id.pointsTextView)
        buttonOneSummon = findViewById(R.id.buttonOneSummon)
        buttonBack = findViewById(R.id.buttonBack)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: "default_user"

        loadPoints()

        buttonOneSummon.setOnClickListener {
            if (points >= 15) {
                performSummon(1)
            } else {
                Toast.makeText(this, "Not enough points for a summon!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadPoints() {
        val userPointsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddiePoints")


        userPointsRef.get().addOnSuccessListener { snapshot ->
            points = snapshot.getValue(Int::class.java) ?: 0
            updatePointsDisplay()
        }
    }


    private fun updatePointsDisplay() {
        pointsTextView.text = "$points"
    }

    private fun performSummon(times: Int) {
        points -= 15
        val userPointsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddiePoints")

        userPointsRef.setValue(points).addOnSuccessListener {
            Toast.makeText(this, "Summoned $times time(s)! Points left: $points", Toast.LENGTH_SHORT).show()
            updatePointsDisplay()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to deduct points: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}