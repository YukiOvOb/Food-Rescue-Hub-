package com.example.foodrescuehub.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodrescuehub.R
import com.example.foodrescuehub.data.model.CartItem
import com.example.foodrescuehub.util.UrlUtils

class CartAdapter(
    private val onIncreaseClick: (Long) -> Unit,
    private val onDecreaseClick: (Long) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onIncreaseClick, onDecreaseClick, onRemoveItem)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = getItem(position)
            for (payload in payloads) {
                if (payload == PAYLOAD_QUANTITY_CHANGED) {
                    holder.updateQuantity(item)
                }
            }
        }
    }

    class CartViewHolder(
        itemView: View,
        private val onIncreaseClick: (Long) -> Unit,
        private val onDecreaseClick: (Long) -> Unit,
        private val onRemoveItem: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivItemImage: ImageView = itemView.findViewById(R.id.ivCartItemImage)
        private val tvItemTitle: TextView = itemView.findViewById(R.id.tvCartItemTitle)
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvCartStoreName)
        private val tvItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvCartQuantity)
        private val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecreaseQuantity)
        private val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncreaseQuantity)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveItem)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvCartSubtotal)

        private var currentListingId: Long = -1

        fun bind(item: CartItem) {
            currentListingId = item.listingId

            // USE UrlUtils to construct absolute image URL
            val imageUrl = UrlUtils.getFullUrl(item.photoUrl)
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(ivItemImage)

            tvItemTitle.text = item.title
            tvStoreName.text = item.storeName
            tvItemPrice.text = "$%.2f".format(item.price)
            updateQuantity(item)
        }

        init {
            btnDecrease.setOnClickListener {
                if (currentListingId != -1L) onDecreaseClick(currentListingId)
            }
            btnIncrease.setOnClickListener {
                if (currentListingId != -1L) onIncreaseClick(currentListingId)
            }
        }

        fun updateQuantity(item: CartItem) {
            currentListingId = item.listingId
            tvQuantity.text = item.quantity.toString()
            tvSubtotal.text = "$%.2f".format(item.getSubtotal())
            btnDecrease.isEnabled = item.quantity > 1
            btnIncrease.isEnabled = item.quantity < item.maxQuantity
            btnRemove.setOnClickListener { onRemoveItem(item) }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.listingId == newItem.listingId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: CartItem, newItem: CartItem): Any? {
            return if (oldItem.quantity != newItem.quantity) PAYLOAD_QUANTITY_CHANGED else null
        }
    }

    companion object {
        private const val PAYLOAD_QUANTITY_CHANGED = "quantity_changed"
    }
}
