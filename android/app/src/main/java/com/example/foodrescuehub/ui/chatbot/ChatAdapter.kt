package com.example.foodrescuehub.ui.chatbot

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodrescuehub.data.model.HistoryMessage
import com.example.foodrescuehub.databinding.ItemMessageBinding
import androidx.core.graphics.toColorInt

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Helper list to store messages
    private val messages = mutableListOf<HistoryMessage>()

    fun addMessage(message: HistoryMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: HistoryMessage) {
            binding.tvContent.text = msg.content

            // Styling based on who sent the message
            if (msg.role == "user") {
                // User: Right side, Green/Primary Color
                binding.messageContainer.gravity = Gravity.END
                binding.cardBubble.setCardBackgroundColor("#DCF8C6".toColorInt())
            } else {
                // Bot: Left side, White/Gray
                binding.messageContainer.gravity = Gravity.START
                binding.cardBubble.setCardBackgroundColor("#FFFFFF".toColorInt())
            }
        }
    }
}