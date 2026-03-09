package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMeasurementsActivity : AppCompatActivity() {

    private lateinit var btnShirt: MaterialButton
    private lateinit var btnPant: MaterialButton
    private lateinit var btnKoti: MaterialButton
    private lateinit var btnSuit: MaterialButton
    private lateinit var btnJabbho: MaterialButton
    private lateinit var btnLehngho: MaterialButton
    private lateinit var btnSafari: MaterialButton
    private lateinit var btnJodhpuri: MaterialButton
    private lateinit var tvMeasurementTitle: TextView
    private lateinit var tvCustomerName: TextView
    
    private lateinit var etLength: EditText
    private lateinit var etChest: EditText
    private lateinit var etWaist: EditText
    private lateinit var etCollar: EditText
    private lateinit var etShoulder: EditText
    private lateinit var etSleeve: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSaveMeasurements: Button
    
    private lateinit var dbHelper: DatabaseHelper
    private var selectedGarment = "Shirt"
    private var customerMobile = ""
    private var customerId: Int = -1
    private var isEditMode: Boolean = false
    private var lastMeasurementId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_measurements)

        dbHelper = DatabaseHelper(this)

        // Initialize views with the new Profile-suffixed IDs from layout
        btnShirt = findViewById(R.id.btnShirtProfile)
        btnPant = findViewById(R.id.btnPantProfile)
        btnKoti = findViewById(R.id.btnKotiProfile)
        btnSuit = findViewById(R.id.btnSuitProfile)
        btnJabbho = findViewById(R.id.btnJabbhoProfile)
        btnLehngho = findViewById(R.id.btnLehnghoProfile)
        btnSafari = findViewById(R.id.btnSafariProfile)
        btnJodhpuri = findViewById(R.id.btnJodhpuriProfile)
        
        tvMeasurementTitle = findViewById(R.id.tvMeasurementTitle)
        tvCustomerName = findViewById(R.id.tvCustomerName)

        etLength = findViewById(R.id.etLength)
        etChest = findViewById(R.id.etChest)
        etWaist = findViewById(R.id.etWaist)
        etCollar = findViewById(R.id.etCollar)
        etShoulder = findViewById(R.id.etShoulder)
        etSleeve = findViewById(R.id.etSleeve)
        etNotes = findViewById(R.id.etNotes)
        btnSaveMeasurements = findViewById(R.id.btnSaveMeasurements)

        // Get info from intent
        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        customerMobile = intent.getStringExtra("CUSTOMER_MOBILE") ?: ""
        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        tvCustomerName.text = name

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageView>(R.id.btnDeleteMeasurement)?.setOnClickListener {
            // Delete logic can be added here
            Toast.makeText(this, "Delete feature coming soon", Toast.LENGTH_SHORT).show()
        }

        if (isEditMode) loadLatestMeasurement()

        val buttons = listOf(btnShirt, btnPant, btnKoti, btnSuit, btnJabbho, btnLehngho, btnSafari, btnJodhpuri)
        buttons.forEach { button ->
            button.setOnClickListener {
                selectedGarment = button.text.toString()
                selectButton(button, buttons)
                updateTitle(selectedGarment)
                if (isEditMode) loadLatestMeasurement()
            }
        }

        btnSaveMeasurements.setOnClickListener {
            saveAndSyncData()
        }
    }

    private fun loadLatestMeasurement() {
        Thread {
            val cursor = dbHelper.getLatestMeasurement(customerId, selectedGarment)
            if (cursor.moveToFirst()) {
                val lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val length = cursor.getString(cursor.getColumnIndexOrThrow("length"))
                val chest = cursor.getString(cursor.getColumnIndexOrThrow("chest"))
                val waist = cursor.getString(cursor.getColumnIndexOrThrow("waist"))
                val collar = cursor.getString(cursor.getColumnIndexOrThrow("collar"))
                val shoulder = cursor.getString(cursor.getColumnIndexOrThrow("shoulder"))
                val sleeve = cursor.getString(cursor.getColumnIndexOrThrow("sleeve"))
                val notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                
                runOnUiThread {
                    lastMeasurementId = lId
                    etLength.setText(length)
                    etChest.setText(chest)
                    etWaist.setText(waist)
                    etCollar.setText(collar)
                    etShoulder.setText(shoulder)
                    etSleeve.setText(sleeve)
                    etNotes.setText(notes)
                }
            }
            cursor.close()
        }.start()
    }

    private fun saveAndSyncData() {
        val length = etLength.text.toString().trim()
        val chest = etChest.text.toString().trim()
        val waist = etWaist.text.toString().trim()
        val collar = etCollar.text.toString().trim()
        val shoulder = etShoulder.text.toString().trim()
        val sleeve = etSleeve.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val status = "Pending" 

        if (length.isEmpty() || chest.isEmpty()) {
            Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show()
            return
        }

        btnSaveMeasurements.isEnabled = false

        Thread {
            try {
                if (isEditMode && lastMeasurementId != -1) {
                    dbHelper.updateMeasurement(lastMeasurementId, length, chest, waist, collar, shoulder, sleeve, notes, status)
                } else {
                    dbHelper.addMeasurement(customerId, selectedGarment, length, chest, waist, collar, shoulder, sleeve, notes, status)
                }

                runOnUiThread {
                    syncWithBackend(length, chest, waist, collar, shoulder, sleeve, notes, status)
                }
            } catch (e: Exception) {
                Log.e("SAVE_ERROR", "Error: ${e.message}")
                runOnUiThread { btnSaveMeasurements.isEnabled = true }
            }
        }.start()
    }

    private fun syncWithBackend(length: String, chest: String, waist: String, collar: String, shoulder: String, sleeve: String, notes: String, status: String) {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", 1).toString()
        
        val measurementData = mapOf(
            "user_id" to userId,
            "mobile_number" to customerMobile,
            "garment_type" to selectedGarment,
            "length" to length,
            "chest" to chest,
            "waist" to waist,
            "collar" to collar,
            "shoulder" to shoulder,
            "sleeve" to sleeve,
            "notes" to notes,
            "status" to status,
            "is_update" to isEditMode.toString()
        )

        RetrofitClient.instance.addMeasurement(measurementData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                btnSaveMeasurements.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@AddMeasurementsActivity, "Saved & Synced!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddMeasurementsActivity, "Sync failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                btnSaveMeasurements.isEnabled = true
                Toast.makeText(this@AddMeasurementsActivity, "Saved locally (Offline)", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun selectButton(selectedButton: MaterialButton, allButtons: List<MaterialButton>) {
        allButtons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundColor(Color.RED)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(Color.WHITE)
                button.setTextColor(Color.parseColor("#333333"))
            }
        }
    }

    private fun updateTitle(garmentType: String) {
        tvMeasurementTitle.text = "$garmentType Measurements"
    }
}