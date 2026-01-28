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

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onQuantityChanged, onRemoveItem)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        itemView: View,
        private val onQuantityChanged: (CartItem, Int) -> Unit,
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

        fun bind(item: CartItem) {
            if (!item.photoUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(item.photoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(ivItemImage)
            } else {
                ivItemImage.setImageResource(R.drawable.ic_launcher_foreground)
            }

            tvItemTitle.text = item.title
            tvStoreName.text = item.storeName
            tvItemPrice.text = "$%.2f".format(item.price)
            tvQuantity.text = item.quantity.toString()
            tvSubtotal.text = "$%.2f".format(item.getSubtotal())

            btnDecrease.setOnClickListener {
                onQuantityChanged(item, item.quantity - 1)
            }

            btnIncrease.setOnClickListener {
                if (item.quantity < item.maxQuantity) {
                    onQuantityChanged(item, item.quantity + 1)
                }
            }

            btnDecrease.isEnabled = item.quantity > 1
            btnIncrease.isEnabled = item.quantity < item.maxQuantity

            btnRemove.setOnClickListener {
                onRemoveItem(item)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.listingId == newItem.listingId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
