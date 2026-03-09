package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CheckOrderModel(
    val id: String,
    val customerName: String,
    val garmentType: String // Changed from status to show item name
)

class CheckOrderAdapter(
    private var orders: List<CheckOrderModel>
) : RecyclerView.Adapter<CheckOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvCheckOrderId)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCheckCustomerName)
        val tvGarmentType: TextView = view.findViewById(R.id.tvCheckStatus) // Uses status badge UI for garment name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderId.text = "#" + order.id.padStart(3, '0')
        holder.tvCustomerName.text = order.customerName
        holder.tvGarmentType.text = order.garmentType
        
        // Always show in professional Red theme for garment identification
        holder.tvGarmentType.setBackgroundResource(R.drawable.bg_black_rounded)
        holder.tvGarmentType.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF0F0"))
        holder.tvGarmentType.setTextColor(android.graphics.Color.parseColor("#FF0000"))
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<CheckOrderModel>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}