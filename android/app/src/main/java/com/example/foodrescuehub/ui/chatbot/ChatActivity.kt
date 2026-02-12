package com.example.foodrescuehub.ui.chatbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodrescuehub.data.api.RetrofitClient
import com.example.foodrescuehub.data.model.ChatRequest
import com.example.foodrescuehub.data.model.HistoryMessage
import com.example.foodrescuehub.databinding.ActivityChatBotBinding
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBotBinding
    private val chatAdapter = ChatAdapter()

    // We keep a history list to send to the AI context
    private val messageHistory = mutableListOf<HistoryMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupWindowInsets()
    }

    private fun setupWindowInsets() {
        val inputBar = binding.inputBarContainer
        val initialBottomPadding = inputBar.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(inputBar) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialBottomPadding + navBarInset
            )
            insets
        }
        ViewCompat.requestApplyInsets(inputBar)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish() // Go back to profile
        }
    }

    private fun setupRecyclerView() {
        binding.rvChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // Start from bottom like a real chat
            }
        }

        // Add Welcome Message
        addBotMessage("Hello! I am RescueBot. How can I help you save food today?")
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(content: String) {
        // 1. Show User message on UI immediately
        val userMsg = HistoryMessage("user", content)
        chatAdapter.addMessage(userMsg)
        messageHistory.add(userMsg)

        // Clear input
        binding.etMessage.text.clear()
        scrollToBottom()

        // 2. Send to API
        lifecycleScope.launch {
            try {
                // NOTE: Using rescueBotApi (Port 8000)
                val response = RetrofitClient.rescueBotApi.sendMessage(
                    ChatRequest(message = content, history = messageHistory)
                )

                if (response.isSuccessful && response.body() != null) {
                    val reply = response.body()!!.reply
                    addBotMessage(reply)
                } else {
                    addBotMessage("Error: Could not reach RescueBot.")
                }
            } catch (e: Exception) {
                addBotMessage("Connection failed. Is the server running?")
                e.printStackTrace()
            }
        }
    }

    private fun addBotMessage(content: String) {
        val botMsg = HistoryMessage("assistant", content)
        chatAdapter.addMessage(botMsg)
        messageHistory.add(botMsg)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }
}
