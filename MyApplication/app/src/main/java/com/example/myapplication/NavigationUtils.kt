package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setupGlobalNavigation(activity: Activity, currentItemId: Int) {
    this.selectedItemId = currentItemId
    this.setOnItemSelectedListener { item ->
        if (item.itemId == currentItemId) return@setOnItemSelectedListener true

        when (item.itemId) {
            R.id.nav_home -> {
                activity.startActivity(Intent(activity, HomeActivity::class.java))
                if (activity !is HomeActivity) activity.finish()
                true
            }
            R.id.nav_customers -> {
                activity.startActivity(Intent(activity, AddCustomerActivity::class.java))
                if (activity !is AddCustomerActivity) activity.finish()
                true
            }
            R.id.nav_reports -> {
                activity.startActivity(Intent(activity, DashboardActivity::class.java))
                if (activity !is DashboardActivity) activity.finish()
                true
            }
            else -> false
        }
    }
}