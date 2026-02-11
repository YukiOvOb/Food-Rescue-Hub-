package com.example.foodrescuehub.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying orders in RecyclerView
 */
class OrdersAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, onOrderClick)
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val tvPickupTime: TextView = itemView.findViewById(R.id.tvPickupTime)
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val tvStoreAddress: TextView = itemView.findViewById(R.id.tvStoreAddress)
        private val llOrderItems: LinearLayout = itemView.findViewById(R.id.llOrderItems)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)

        fun bind(order: Order, onOrderClick: (Order) -> Unit) {
            // Set click listener
            itemView.setOnClickListener { onOrderClick(order) }
            // Order ID
            tvOrderId.text = "Order #${order.orderId}"

            // Order Status
            tvOrderStatus.text = order.status
            tvOrderStatus.setBackgroundResource(getStatusBackground(order.status))

            // Order Date
            tvOrderDate.text = "Placed on ${formatDate(order.createdAt)}"

            // Pickup Time
            val pickupTime = if (order.pickupSlotStart != null && order.pickupSlotEnd != null) {
                "${formatTime(order.pickupSlotStart)} - ${formatTime(order.pickupSlotEnd)}"
            } else {
                "Not specified"
            }
            tvPickupTime.text = pickupTime

            // Store Name and Address
            tvStoreName.text = order.store?.storeName ?: "Store #${order.store?.storeId ?: "N/A"}"
            tvStoreAddress.text = order.store?.addressLine ?: "Address not available"

            // Order Items
            llOrderItems.removeAllViews()
            order.orderItems?.forEach { item ->
                val itemView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_order_item, llOrderItems, false)

                val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
                val tvItemQuantity = itemView.findViewById<TextView>(R.id.tvItemQuantity)
                val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

                tvItemName.text = item.listing?.title ?: "Item"
                tvItemQuantity.text = "Qty: ${item.quantity} × $${String.format("%.2f", item.unitPrice)}"
                tvItemPrice.text = "$${String.format("%.2f", item.lineTotal)}"

                llOrderItems.addView(itemView)
            }

            // Total Amount
            tvTotalAmount.text = "$${String.format("%.2f", order.totalAmount)}"
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun formatTime(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun getStatusBackground(status: String): Int {
            return when (status.uppercase()) {
                "PENDING_PAYMENT", "PENDING" -> R.drawable.bg_status_pending
                "PAID" -> R.drawable.bg_status_confirmed
                "ACCEPTED", "CONFIRMED", "READY" -> R.drawable.bg_status_confirmed
                "COMPLETED" -> R.drawable.bg_status_completed
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
