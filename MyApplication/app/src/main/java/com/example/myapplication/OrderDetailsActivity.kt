package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class OrderDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        // Setup the bottom navigation bar
        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_reports)

        // Find views
        val tvCustomerName = findViewById<TextView>(R.id.tvOrderCustomerName)
        val tvCustomerId = findViewById<TextView>(R.id.tvOrderCustomerId)
        val tvGarmentName = findViewById<TextView>(R.id.tvOrderGarmentName)
        val tvStatus = findViewById<TextView>(R.id.tvOrderStatus)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Retrieve passed data
        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "N/A"
        val id = intent.getStringExtra("CUSTOMER_ID") ?: "N/A"
        val garment = intent.getStringExtra("GARMENT_NAME") ?: "N/A"
        val status = intent.getStringExtra("STATUS") ?: "Pending"

        // Set text
        tvCustomerName.text = name
        tvCustomerId.text = "ID: #$id"
        tvGarmentName.text = garment
        tvStatus.text = status

        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}