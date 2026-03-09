package com.example.myapplication

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomerListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        dbHelper = DatabaseHelper(this)
        displayCustomersData()
    }

    private fun displayCustomersData() {
        // Querying strictly from the tailorhub_customer table
        val cursor: Cursor = dbHelper.getAllCustomers()
        val count = cursor.count
        
        Toast.makeText(this, "Total Customers: $count", Toast.LENGTH_LONG).show()
        Log.d("CustomerDataCheck", "Total customers found in tailorhub_customer: $count")
        
        if (count > 0) {
            while (cursor.moveToNext()) {
                val nameIndex = cursor.getColumnIndex("name")
                val mobileIndex = cursor.getColumnIndex("mobile_number")
                val addressIndex = cursor.getColumnIndex("address")
                val lengthIndex = cursor.getColumnIndex("length")

                val name = if (nameIndex != -1) cursor.getString(nameIndex) else "N/A"
                val mobile = if (mobileIndex != -1) cursor.getString(mobileIndex) else "N/A"
                val address = if (addressIndex != -1) cursor.getString(addressIndex) else "N/A"
                val length = if (lengthIndex != -1) cursor.getString(lengthIndex) else "N/A"

                Log.d("CustomerDataCheck", "Customer -> Name: $name, Mobile: $mobile, Address: $address, Length: $length")
            }
        } else {
            Log.d("CustomerDataCheck", "No data found in tailorhub_customer table.")
        }
        cursor.close()
    }
}