package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TailorHub.db"
        private const val DATABASE_VERSION = 12
        
        const val TABLE_USERS = "users"
        const val TABLE_CUSTOMERS = "tailorhub_customer"
        const val TABLE_MEASUREMENTS = "tailorhub_measurements"
        const val TABLE_ORDERS = "tailorhub_order"

        const val COLUMN_ID = "id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_MOBILE = "mobile_number"
        const val COLUMN_PASSWORD = "password"
        
        const val COLUMN_CUST_NAME = "name"
        const val COLUMN_CUST_ADDRESS = "address"
        const val COLUMN_CUST_LENGTH = "length"

        // Measurements Columns
        const val COLUMN_MEAS_CUST_ID = "customer_id"
        const val COLUMN_MEAS_GARMENT_TYPE = "garment_type"
        const val COLUMN_MEAS_LENGTH = "length"
        const val COLUMN_MEAS_CHEST = "chest"
        const val COLUMN_MEAS_WAIST = "waist"
        const val COLUMN_MEAS_COLLAR = "collar"
        const val COLUMN_MEAS_SHOULDER = "shoulder"
        const val COLUMN_MEAS_SLEEVE = "sleeve"
        const val COLUMN_MEAS_NOTES = "notes"
        const val COLUMN_MEAS_STATUS = "status"

        // Order Columns
        const val COLUMN_ORDER_CUST_ID = "customer_id"
        const val COLUMN_ORDER_GARMENT_TYPE = "garment_type"
        const val COLUMN_ORDER_STATUS = "status"
        const val COLUMN_ORDER_DATE = "order_date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_FIRST_NAME TEXT, $COLUMN_LAST_NAME TEXT, $COLUMN_MOBILE TEXT, $COLUMN_PASSWORD TEXT)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_CUSTOMERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_CUST_NAME TEXT, $COLUMN_MOBILE TEXT, $COLUMN_CUST_ADDRESS TEXT, $COLUMN_CUST_LENGTH TEXT)")
        
        val createMeasurementsTable = ("CREATE TABLE IF NOT EXISTS $TABLE_MEASUREMENTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_MEAS_CUST_ID INTEGER, " +
                "$COLUMN_MEAS_GARMENT_TYPE TEXT, " +
                "$COLUMN_MEAS_LENGTH TEXT, " +
                "$COLUMN_MEAS_CHEST TEXT, " +
                "$COLUMN_MEAS_WAIST TEXT, " +
                "$COLUMN_MEAS_COLLAR TEXT, " +
                "$COLUMN_MEAS_SHOULDER TEXT, " +
                "$COLUMN_MEAS_SLEEVE TEXT, " +
                "$COLUMN_MEAS_NOTES TEXT, " +
                "$COLUMN_MEAS_STATUS TEXT, " +
                "FOREIGN KEY($COLUMN_MEAS_CUST_ID) REFERENCES $TABLE_CUSTOMERS($COLUMN_ID))")
        db?.execSQL(createMeasurementsTable)

        val createOrdersTable = ("CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_ORDER_CUST_ID INTEGER, " +
                "$COLUMN_ORDER_GARMENT_TYPE TEXT, " +
                "$COLUMN_ORDER_STATUS TEXT, " +
                "$COLUMN_ORDER_DATE TEXT, " +
                "FOREIGN KEY($COLUMN_ORDER_CUST_ID) REFERENCES $TABLE_CUSTOMERS($COLUMN_ID))")
        db?.execSQL(createOrdersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOMERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MEASUREMENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    // --- USER METHODS ---
    fun addUser(firstName: String, lastName: String, mobile: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_MOBILE, mobile)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    fun getAllUsers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS", null)
    }

    // --- CUSTOMER METHODS ---
    fun addCustomer(name: String, mobile: String, address: String, length: String = ""): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CUST_NAME, name)
            put(COLUMN_MOBILE, mobile)
            put(COLUMN_CUST_ADDRESS, address)
            put(COLUMN_CUST_LENGTH, length)
        }
        val result = db.insert(TABLE_CUSTOMERS, null, values)
        db.close()
        return result
    }

    fun getAllCustomers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_CUSTOMERS", null)
    }

    fun getTotalCustomerCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CUSTOMERS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun deleteCustomer(mobile: String): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_CUSTOMERS, "$COLUMN_MOBILE = ?", arrayOf(mobile))
        db.close()
        return result
    }

    // --- MEASUREMENT METHODS ---
    fun addMeasurement(customerId: Int, type: String, length: String, chest: String, waist: String, collar: String, shoulder: String, sleeve: String, notes: String, status: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MEAS_CUST_ID, customerId)
            put(COLUMN_MEAS_GARMENT_TYPE, type)
            put(COLUMN_MEAS_LENGTH, length)
            put(COLUMN_MEAS_CHEST, chest)
            put(COLUMN_MEAS_WAIST, waist)
            put(COLUMN_MEAS_COLLAR, collar)
            put(COLUMN_MEAS_SHOULDER, shoulder)
            put(COLUMN_MEAS_SLEEVE, sleeve)
            put(COLUMN_MEAS_NOTES, notes)
            put(COLUMN_MEAS_STATUS, status)
        }
        val result = db.insert(TABLE_MEASUREMENTS, null, values)
        
        // Also add to orders table using the same open db connection (avoids double-open bug)
        addOrder(db, customerId, type, status)
        
        db.close()
        return result
    }

    private fun addOrder(db: SQLiteDatabase, customerId: Int, garmentType: String, status: String): Long {
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put(COLUMN_ORDER_CUST_ID, customerId)
            put(COLUMN_ORDER_GARMENT_TYPE, garmentType)
            put(COLUMN_ORDER_STATUS, status)
            put(COLUMN_ORDER_DATE, currentDate)
        }
        return db.insert(TABLE_ORDERS, null, values)
    }

    fun getOrderCountByStatus(status: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_STATUS = ?", arrayOf(status))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getTotalOrderCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ORDERS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getLatestMeasurement(customerId: Int, garmentType: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_MEASUREMENTS WHERE $COLUMN_MEAS_CUST_ID = ? AND $COLUMN_MEAS_GARMENT_TYPE = ? ORDER BY $COLUMN_ID DESC LIMIT 1",
            arrayOf(customerId.toString(), garmentType)
        )
    }

    fun updateMeasurement(id: Int, length: String, chest: String, waist: String, collar: String, shoulder: String, sleeve: String, notes: String, status: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MEAS_LENGTH, length)
            put(COLUMN_MEAS_CHEST, chest)
            put(COLUMN_MEAS_WAIST, waist)
            put(COLUMN_MEAS_COLLAR, collar)
            put(COLUMN_MEAS_SHOULDER, shoulder)
            put(COLUMN_MEAS_SLEEVE, sleeve)
            put(COLUMN_MEAS_NOTES, notes)
            put(COLUMN_MEAS_STATUS, status)
        }
        val result = db.update(TABLE_MEASUREMENTS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getAllOrdersWithDetails(): Cursor {
        val db = this.readableDatabase
        val query = "SELECT o.$COLUMN_ID as order_id, c.$COLUMN_CUST_NAME as cust_name, o.$COLUMN_ORDER_GARMENT_TYPE as garment_type " +
                    "FROM $TABLE_ORDERS o " +
                    "JOIN $TABLE_CUSTOMERS c ON o.$COLUMN_ORDER_CUST_ID = c.$COLUMN_ID " +
                    "ORDER BY o.$COLUMN_ID DESC"
        return db.rawQuery(query, null)
    }
}