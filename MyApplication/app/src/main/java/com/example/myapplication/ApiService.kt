package com.example.myapplication

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Data model for Registration
data class User(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// Data model for Customer
data class CustomerResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("address") val address: String?,
    @SerializedName("length") val length: String?
)

// Data model for Login
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("user_id") val userId: Int?
)

// NEW: Data model for Order Status response
data class OrderStatusResponse(
    @SerializedName("id") val id: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("status") val status: String,
    @SerializedName("estimated_completion_date") val estimatedCompletionDate: String?
)

interface ApiService {
    @POST("api/register/") 
    fun registerUser(@Body user: User): Call<Void>

    @POST("api/login/")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/add_customer/")
    fun addCustomer(@Body customerData: Map<String, String>): Call<Void>

    @GET("api/get_customers/")
    fun getAllCustomers(): Call<List<CustomerResponse>>

    @DELETE("api/delete_customer/{mobile}/")
    fun deleteCustomer(@Path("mobile") mobile: String): Call<Void>

    @POST("api/add_measurement/")
    fun addMeasurement(@Body measurementData: Map<String, String>): Call<Void>

    // NEW: Get order status by ID
    @GET("api/orders/{orderId}/")
    fun getOrderStatus(@Path("orderId") orderId: String): Call<OrderStatusResponse>
}