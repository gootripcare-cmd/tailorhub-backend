package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCustomerActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_customer)

        dbHelper = DatabaseHelper(this)

        val etCustomerName = findViewById<EditText>(R.id.etCustomerName)
        val etMobileNumber = findViewById<EditText>(R.id.etMobileNumber)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val btnSaveCustomer = findViewById<Button>(R.id.btnSaveCustomer)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // --- NAVIGATION BAR LOGIC ---
        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_customers)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnSaveCustomer.setOnClickListener {
            val name = etCustomerName.text.toString().trim()
            val mobile = etMobileNumber.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Name and Mobile are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveCustomer.isEnabled = false

            // 1. Save to local SQLite first and get the generated ID
            Thread {
                val customerId = dbHelper.addCustomer(name, mobile, address)

                runOnUiThread {
                    if (customerId != -1L) {
                        // 2. Attempt to sync with Django backend
                        val customerData = mapOf(
                            "name" to name,
                            "mobile_number" to mobile,
                            "address" to address
                        )

                        RetrofitClient.instance.addCustomer(customerData)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    btnSaveCustomer.isEnabled = true
                                    Toast.makeText(this@AddCustomerActivity, "Customer Saved!", Toast.LENGTH_SHORT).show()
                                    navigateToMeasurements(name, mobile, customerId.toInt())
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    btnSaveCustomer.isEnabled = true
                                    Toast.makeText(this@AddCustomerActivity, "Saved locally (Offline)", Toast.LENGTH_SHORT).show()
                                    navigateToMeasurements(name, mobile, customerId.toInt())
                                }
                            })
                    } else {
                        btnSaveCustomer.isEnabled = true
                        Toast.makeText(this@AddCustomerActivity, "Local Database Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun navigateToMeasurements(name: String, mobile: String, id: Int) {
        val intent = Intent(this@AddCustomerActivity, AddMeasurementsActivity::class.java)
        intent.putExtra("CUSTOMER_NAME", name)
        intent.putExtra("CUSTOMER_MOBILE", mobile)
        intent.putExtra("CUSTOMER_ID", id) // Pass the actual ID from DB
        startActivity(intent)
        finish()
    }
}