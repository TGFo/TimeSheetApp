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
import kotlin.random.Random

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
        gachaImage = findViewById(R.id.gachaImage)

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

        // Update points in the database
        userPointsRef.setValue(points).addOnSuccessListener {
            Toast.makeText(this, "Summoned $times time(s)! Points left: $points", Toast.LENGTH_SHORT).show()
            updatePointsDisplay()

            // Perform the Gacha draw
            val chuddieName = drawChuddie()
            updateChuddieCount(chuddieName)

            updateGachaImage(chuddieName)
            // Display the result
            Toast.makeText(this, "You got: $chuddieName!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to deduct points: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    // Function to determine the Chuddie based on probability
    private fun drawChuddie(): String {
        val randomValue = Random.nextInt(100) // Random value between 0 and 99

        return when {
            randomValue < 25 -> "chudJack"
            randomValue < 50 -> "midLifer"
            randomValue < 75 -> "serenity"
            randomValue < 85 -> "gigaChud"
            randomValue < 95 -> "femcel"
            else -> "soylessOne"
        }
    }

    // Function to update the count of the Chuddie in the database
    private fun updateChuddieCount(chuddieName: String) {
        val chuddieRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddies").child(chuddieName)

        chuddieRef.get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            val newCount = currentCount + 1

            // Update the count in Firebase
            chuddieRef.setValue(newCount).addOnSuccessListener {
                Toast.makeText(this, "$chuddieName count updated to $newCount", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update $chuddieName count: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch $chuddieName count: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateGachaImage(chuddieName: String) {
        val imageResource = when (chuddieName) {
            "chudJack" -> R.drawable.base_chuddie
            "midLifer" -> R.drawable.balding_chuddie
            "serenity" -> R.drawable.serene_chuddie
            "gigaChud" -> R.drawable.giga_chuddie
            "femcel" -> R.drawable.femcell_chuddie
            "soylessOne" -> R.drawable.soyless_chuddie
            else -> R.drawable.hidden_chuddie // Default image if something goes wrong
        }

        // Update the image view
        gachaImage.setImageResource(imageResource)
    }
}