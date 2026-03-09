package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    private lateinit var tvCustomerListTitle: TextView
    private lateinit var rvCustomers: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CustomerAdapter
    private lateinit var switchDevMode: SwitchMaterial
    private lateinit var etSearch: TextInputEditText
    
    private var allCustomers = mutableListOf<CustomerDisplayModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)
        tvCustomerListTitle = findViewById(R.id.tvCustomerListTitle)
        rvCustomers = findViewById(R.id.rvCustomers)
        switchDevMode = findViewById(R.id.switchDevMode)
        etSearch = findViewById(R.id.etSearch)

        // Setup Dev Mode Logic
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isDevMode = sharedPref.getBoolean("DEV_MODE", false)
        switchDevMode.isChecked = isDevMode

        switchDevMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("DEV_MODE", isChecked).apply()
            val status = if (isChecked) "ON (Login/Register Disabled)" else "OFF (Login/Register Enabled)"
            Toast.makeText(this, "Development Mode: $status", Toast.LENGTH_SHORT).show()
        }

        adapter = CustomerAdapter(emptyList(), { customer ->
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("CUSTOMER_NAME", customer.name)
            intent.putExtra("CUSTOMER_MOBILE", customer.mobile)
            startActivity(intent)
        }, { mobile ->
            showDeleteConfirmation(mobile)
        })
        rvCustomers.adapter = adapter

        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_home)

        setupAlphabetFilters()
        setupSearch()
        refreshAllData()
    }

    override fun onResume() {
        super.onResume()
        refreshAllData()
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_home
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilter(text: String) {
        val filteredList = if (text.isEmpty()) {
            allCustomers
        } else {
            allCustomers.filter { 
                it.name.lowercase().contains(text.lowercase()) || 
                it.mobile.contains(text) 
            }
        }
        adapter.updateData(filteredList)
        tvCustomerListTitle.text = if (text.isEmpty()) "All Customers" else "Search Results (${filteredList.size})"
    }

    private fun refreshAllData() {
        RetrofitClient.instance.getAllCustomers().enqueue(object : Callback<List<CustomerResponse>> {
            override fun onResponse(call: Call<List<CustomerResponse>>, response: Response<List<CustomerResponse>>) {
                if (response.isSuccessful) {
                    val backendData = response.body() ?: emptyList()
                    val newList = backendData.map { 
                        CustomerDisplayModel(it.id.toString(), it.name, it.mobileNumber, it.length ?: "") 
                    }
                    updateUI(newList, " (Synced)")
                } else {
                    Log.e("HOME_DEBUG", "Backend fetch failed: ${response.code()}")
                    fetchFromLocal()
                }
            }

            override fun onFailure(call: Call<List<CustomerResponse>>, t: Throwable) {
                Log.e("HOME_DEBUG", "Network failure", t)
                fetchFromLocal()
            }
        })
    }

    private fun fetchFromLocal() {
        thread {
            val cursor = dbHelper.getAllCustomers()
            val list = mutableListOf<CustomerDisplayModel>()
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile_number"))
                    val length = cursor.getString(cursor.getColumnIndexOrThrow("length"))
                    list.add(CustomerDisplayModel(id, name, mobile, length))
                } while (cursor.moveToNext())
            }
            cursor.close()
            runOnUiThread { updateUI(list, " (Local)") }
        }
    }

    private fun updateUI(list: List<CustomerDisplayModel>, source: String) {
        allCustomers = list.sortedBy { it.name.lowercase() }.toMutableList()
        
        // If user is currently searching, respect that filter
        val searchQuery = etSearch.text.toString()
        if (searchQuery.isNotEmpty()) {
            applyFilter(searchQuery)
        } else {
            tvCustomerListTitle.text = "All Customers$source"
            adapter.updateData(allCustomers)
        }
    }

    private fun setupAlphabetFilters() {
        findViewById<Button>(R.id.btnAll)?.setOnClickListener {
            etSearch.text?.clear()
            tvCustomerListTitle.text = "All Customers"
            adapter.updateData(allCustomers)
        }

        val alphabet = ('A'..'Z').toList()
        alphabet.forEach { letter ->
            val resId = resources.getIdentifier("btn$letter", "id", packageName)
            if (resId != 0) {
                findViewById<Button>(resId).setOnClickListener {
                    etSearch.text?.clear()
                    val filtered = allCustomers.filter { it.name.startsWith(letter.toString(), ignoreCase = true) }
                    tvCustomerListTitle.text = "Customers - $letter"
                    adapter.updateData(filtered)
                }
            }
        }
        tvCustomerListTitle.setOnClickListener { refreshAllData() }
    }

    private fun showDeleteConfirmation(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Customer")
            .setMessage("Permanently remove this customer?")
            .setPositiveButton("Delete") { _, _ -> performFullDelete(mobile) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performFullDelete(mobile: String) {
        RetrofitClient.instance.deleteCustomer(mobile).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                deleteLocally(mobile)
                Toast.makeText(this@HomeActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                deleteLocally(mobile)
                Toast.makeText(this@HomeActivity, "Deleted locally (Offline)", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteLocally(mobile: String) {
        thread {
            dbHelper.deleteCustomer(mobile)
            runOnUiThread { refreshAllData() }
        }
    }
}