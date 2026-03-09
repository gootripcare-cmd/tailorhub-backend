package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.concurrent.thread

class CheckOrderActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvCheckOrders: RecyclerView
    private lateinit var adapter: CheckOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_order)

        dbHelper = DatabaseHelper(this)
        rvCheckOrders = findViewById(R.id.rvCheckOrders)

        adapter = CheckOrderAdapter(emptyList())
        rvCheckOrders.adapter = adapter

        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar?.setupGlobalNavigation(this, R.id.nav_reports)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadAllOrders()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_reports
        loadAllOrders()
    }

    private fun loadAllOrders() {
        thread {
            try {
                // Warning fixed: Removed redundant null check as this method returns non-null Cursor
                val cursor = dbHelper.getAllOrdersWithDetails()
                val list = mutableListOf<CheckOrderModel>()
                
                if (cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndex("order_id")
                    val nameCol = cursor.getColumnIndex("cust_name")
                    val statusCol = cursor.getColumnIndex("garment_type")

                    do {
                        val id = if (idCol != -1) cursor.getString(idCol) else "0"
                        val name = if (nameCol != -1) cursor.getString(nameCol) else "Unknown"
                        val status = if (statusCol != -1) cursor.getString(statusCol) else "Pending"
                        
                        list.add(CheckOrderModel(id, name, status))
                    } while (cursor.moveToNext())
                }
                cursor.close()
                
                runOnUiThread {
                    adapter.updateData(list)
                }
            } catch (e: Exception) {
                Log.e("CHECK_ORDER_ERROR", "Failed to load orders", e)
            }
        }
    }
}