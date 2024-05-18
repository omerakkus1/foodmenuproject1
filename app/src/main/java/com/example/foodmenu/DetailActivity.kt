package com.example.foodmenu

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.util.Log
import kotlinx.coroutines.*

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val date = intent.getStringExtra("date")
        if (date == null) {
            Log.e("DetailActivity", "No date provided, finishing activity")
            Toast.makeText(this, "No date provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val textView = findViewById<TextView>(R.id.menuDetailsText)
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val database = FirebaseDatabase.getInstance()
        val menuRef = database.getReference("menus/$date")

        loadMenuDetails(menuRef, textView, date)
    }

    private fun loadMenuDetails(menuRef: DatabaseReference, textView: TextView, date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val menu = snapshot.getValue(Menu::class.java)
                    CoroutineScope(Dispatchers.Main).launch {
                        if (menu != null) {
                            Log.d("DetailActivity", "Menu data loaded")
                            textView.text = "Kahvaltı: ${menu.breakfast}\nÖğle Yemeği: ${menu.lunch}\nAkşam Yemeği: ${menu.dinner}"
                        } else {
                            Log.d("DetailActivity", "No menu found for date: $date")
                            textView.text = "Bu tarihe ait menü bulunamadı."
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.e("DetailActivity", "Error loading menu details: ${error.message}", error.toException())
                        Toast.makeText(this@DetailActivity, "Failed to load menu details.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}
