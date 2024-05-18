package com.example.foodmenu

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*

class MenuActivity : AppCompatActivity() {
    private lateinit var dateButtonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        dateButtonsContainer = findViewById(R.id.dateButtonsContainer)
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val database = FirebaseDatabase.getInstance()
        val datesRef = database.getReference("menus")

        loadData(datesRef)
    }

    private fun loadData(datesRef: DatabaseReference) {
        CoroutineScope(Dispatchers.IO).launch {
            datesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("MenuActivity", "Data found in Firebase")
                        CoroutineScope(Dispatchers.Main).launch {
                            snapshot.children.forEach { dateSnapshot ->
                                val dateKey = dateSnapshot.key ?: return@forEach
                                Log.d("MenuActivity", "Creating button for date: $dateKey")
                                createButton(dateKey)
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("MenuActivity", "No dates found in Firebase")
                            Toast.makeText(this@MenuActivity, "No dates available.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.e("MenuActivity", "Error loading data from Firebase: ${error.message}", error.toException())
                        Toast.makeText(this@MenuActivity, "Failed to load dates.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun createButton(dateKey: String) {
        val button = Button(this@MenuActivity).apply {
            text = dateKey
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val intent = Intent(this@MenuActivity, DetailActivity::class.java)
                intent.putExtra("date", dateKey)
                startActivity(intent)
                Log.d("MenuActivity", "Button clicked for date: $dateKey")
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            dateButtonsContainer.addView(button)
        }
    }
}
