package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import kotlin.concurrent.thread

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recentOrdersAdapter: CheckOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dbHelper = DatabaseHelper(this)

        // Setup bottom navigation
        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_reports)

        // Handle Quick Actions
        findViewById<MaterialCardView>(R.id.activity_check_order)?.setOnClickListener {
            startActivity(Intent(this, CheckOrderActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardAddCustomer)?.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        // Handle Logout
        findViewById<ShapeableImageView>(R.id.ivProfile)?.setOnClickListener {
            logout()
        }

        // Setup Recent Orders RecyclerView
        val rvRecentOrders = findViewById<RecyclerView>(R.id.rvRecentOrders)
        recentOrdersAdapter = CheckOrderAdapter(emptyList())
        rvRecentOrders.adapter = recentOrdersAdapter

        updateStats()
        loadRecentOrders()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        loadRecentOrders()
    }

    private fun updateStats() {
        val tvActive = findViewById<TextView>(R.id.tvActiveOrders)
        val tvPending = findViewById<TextView>(R.id.tvPendingOrders)
        val tvCompleted = findViewById<TextView>(R.id.tvCompletedOrders)
        val tvTotalCust = findViewById<TextView>(R.id.tvTotalCustomersCount)

        tvActive?.text = dbHelper.getTotalOrderCount().toString()
        tvPending?.text = dbHelper.getOrderCountByStatus("Pending").toString()
        tvCompleted?.text = dbHelper.getOrderCountByStatus("Completed").toString()
        tvTotalCust?.text = dbHelper.getTotalCustomerCount().toString()
    }

    private fun loadRecentOrders() {
        thread {
            try {
                val cursor = dbHelper.getAllOrdersWithDetails()
                val list = mutableListOf<CheckOrderModel>()

                if (cursor.moveToFirst()) {
                    val idCol   = cursor.getColumnIndex("order_id")
                    val nameCol = cursor.getColumnIndex("cust_name")
                    val typeCol = cursor.getColumnIndex("garment_type")
                    var count = 0
                    do {
                        val id      = if (idCol   != -1) cursor.getString(idCol)   else "0"
                        val name    = if (nameCol  != -1) cursor.getString(nameCol)  else "Unknown"
                        val garment = if (typeCol  != -1) cursor.getString(typeCol)  else "-"
                        list.add(CheckOrderModel(id, name, garment))
                        count++
                    } while (cursor.moveToNext() && count < 5) // Show only latest 5
                }
                cursor.close()

                runOnUiThread {
                    recentOrdersAdapter.updateData(list)
                }
            } catch (e: Exception) {
                android.util.Log.e("DASHBOARD", "Failed to load recent orders", e)
            }
        }
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
