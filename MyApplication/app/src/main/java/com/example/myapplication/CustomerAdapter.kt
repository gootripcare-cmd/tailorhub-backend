package com.example.myapplication

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

// Data model for customer list items with status
data class CustomerDisplayModel(
    val id: String,
    val name: String,
    val mobile: String,
    val length: String,
    val status: String = "Pending"
)

class CustomerAdapter(
    private var customers: List<CustomerDisplayModel>,
    private val onItemClick: (CustomerDisplayModel) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView? = view.findViewById(R.id.tvId)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvMobile: TextView = view.findViewById(R.id.tvMobile)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvId?.text = "#" + customer.id.padStart(3, '0')
        holder.tvName.text = customer.name
        holder.tvMobile.text = customer.mobile
        
        // Status Binding logic
        holder.chipStatus.text = customer.status
        val statusColor = when (customer.status.lowercase()) {
            "pending" -> Color.parseColor("#FFA500")      // Orange
            "in progress" -> Color.parseColor("#2196F3") // Blue
            "ready" -> Color.parseColor("#4CAF50")       // Green
            else -> Color.GRAY
        }
        
        holder.chipStatus.setChipBackgroundColorResource(android.R.color.transparent)
        holder.chipStatus.setChipStrokeColor(ColorStateList.valueOf(statusColor))
        holder.chipStatus.setTextColor(statusColor)
        holder.chipStatus.chipStrokeWidth = 2f

        holder.itemView.setOnClickListener {
            onItemClick(customer)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(customer.mobile)
        }
    }

    override fun getItemCount() = customers.size

    fun updateData(newCustomers: List<CustomerDisplayModel>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}