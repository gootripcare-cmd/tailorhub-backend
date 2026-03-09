package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class CustomerProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var customerId: Int = -1
    
    // UI References
    private var tvCustomerId: TextView? = null
    private var tvStatus: TextView? = null
    private var tvName: TextView? = null
    private var tvMobile: TextView? = null
    private var tvInitials: TextView? = null
    
    private var tvLengthValue: TextView? = null
    private var tvChestValue: TextView? = null
    private var tvWaistValue: TextView? = null
    private var tvCollarValue: TextView? = null
    private var tvShoulderValue: TextView? = null
    private var tvSleeveValue: TextView? = null
    private var tvNotesValue: TextView? = null
    private var tvMeasurementTitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_profile)

        dbHelper = DatabaseHelper(this)

        // Initialize views
        tvName = findViewById(R.id.tvName)
        tvMobile = findViewById(R.id.tvMobile)
        tvCustomerId = findViewById(R.id.tvCustomerId)
        tvStatus = findViewById(R.id.tvStatus)
        tvInitials = findViewById(R.id.tvInitials)
        
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnEditSize = findViewById<TextView>(R.id.btnEditSize)
        val btnNewOrder = findViewById<Button>(R.id.btnNewOrder)
        
        tvLengthValue = findViewById(R.id.tvLengthValue)
        tvChestValue = findViewById(R.id.tvChestValue)
        tvWaistValue = findViewById(R.id.tvWaistValue)
        tvCollarValue = findViewById(R.id.tvCollarValue)
        tvShoulderValue = findViewById(R.id.tvShoulderValue)
        tvSleeveValue = findViewById(R.id.tvSleeveValue)
        tvNotesValue = findViewById(R.id.tvNotesValue)
        tvMeasurementTitle = findViewById(R.id.tvMeasurementTitle)

        // Retrieve data
        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "Unknown"
        val mobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""

        tvName?.text = name
        tvMobile?.text = mobile
        
        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
        tvInitials?.text = if (initials.isNotEmpty()) initials else "C"

        fetchCustomerData(mobile)

        btnBack?.setOnClickListener { finish() }

        val navigateToMeasurements: (Boolean) -> Unit = { isEdit ->
            val nextIntent = Intent(this, AddMeasurementsActivity::class.java)
            nextIntent.putExtra("CUSTOMER_NAME", name)
            nextIntent.putExtra("CUSTOMER_MOBILE", mobile)
            nextIntent.putExtra("CUSTOMER_ID", customerId)
            nextIntent.putExtra("IS_EDIT_MODE", isEdit)
            startActivity(nextIntent)
        }

        btnEditSize?.setOnClickListener { navigateToMeasurements(true) }
        btnNewOrder?.setOnClickListener { navigateToMeasurements(false) }
        
        setupGarmentButtons()
    }

    override fun onResume() {
        super.onResume()
        val mobile = tvMobile?.text.toString()
        if (mobile.isNotEmpty()) fetchCustomerData(mobile)
    }

    private fun fetchCustomerData(mobile: String) {
        thread {
            try {
                val cursor = dbHelper.getAllCustomers()
                if (cursor.moveToFirst()) {
                    do {
                        val mobileCol = cursor.getColumnIndex("mobile_number")
                        if (mobileCol != -1 && cursor.getString(mobileCol) == mobile) {
                            customerId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                            runOnUiThread {
                                tvCustomerId?.text = "Customer ID: #%03d".format(customerId)
                                loadLatestMeasurements("Shirt")
                            }
                            break
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            } catch (e: Exception) { Log.e("PROFILE_DEBUG", "Error", e) }
        }
    }

    private fun loadLatestMeasurements(type: String) {
        if (customerId == -1) return
        thread {
            try {
                val cursor = dbHelper.getLatestMeasurement(customerId, type)
                if (cursor.moveToFirst()) {
                    val len = cursor.getString(cursor.getColumnIndexOrThrow("length"))
                    val ch = cursor.getString(cursor.getColumnIndexOrThrow("chest"))
                    val wa = cursor.getString(cursor.getColumnIndexOrThrow("waist"))
                    val col = cursor.getString(cursor.getColumnIndexOrThrow("collar"))
                    val sh = cursor.getString(cursor.getColumnIndexOrThrow("shoulder"))
                    val sl = cursor.getString(cursor.getColumnIndexOrThrow("sleeve"))
                    val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                    val st = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                    runOnUiThread {
                        tvMeasurementTitle?.text = "$type Measurements"
                        tvLengthValue?.text = "$len in"
                        tvChestValue?.text = "$ch in"
                        tvWaistValue?.text = "$wa in"
                        tvCollarValue?.text = "$col in"
                        tvShoulderValue?.text = "$sh in"
                        tvSleeveValue?.text = "$sl in"
                        tvNotesValue?.text = if (notes.isNullOrEmpty()) "None" else notes
                        tvStatus?.text = st
                    }
                } else {
                    runOnUiThread {
                        tvMeasurementTitle?.text = "$type (No Data)"
                        listOf(tvLengthValue, tvChestValue, tvWaistValue, tvCollarValue, tvShoulderValue, tvSleeveValue).forEach { it?.text = "-" }
                        tvNotesValue?.text = "None"
                        tvStatus?.text = "None"
                    }
                }
                cursor.close()
            } catch (e: Exception) { Log.e("PROFILE_DEBUG", "Error", e) }
        }
    }

    private fun setupGarmentButtons() {
        val garmentTypes = listOf("Shirt", "Pant", "Koti", "Suit", "Jabbho", "Lehngho", "Safari", "Jodhpuri")
        thread {
            val root = findViewById<ViewGroup>(android.R.id.content)
            val buttons = mutableListOf<Button>()
            findAllButtons(root, buttons)
            
            runOnUiThread {
                buttons.forEach { btn ->
                    val txt = btn.text.toString()
                    if (garmentTypes.any { it.equals(txt, ignoreCase = true) }) {
                        btn.setOnClickListener { loadLatestMeasurements(txt) }
                    }
                }
            }
        }
    }

    private fun findAllButtons(view: View, list: MutableList<Button>) {
        if (view is Button) list.add(view)
        else if (view is ViewGroup) {
            for (i in 0 until view.childCount) findAllButtons(view.getChildAt(i), list)
        }
    }
}