package com.radwan.nova.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radwan.nova.data.models.Message
import com.radwan.nova.data.models.MessageType
import com.radwan.nova.data.repository.NovaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: NovaRepository,
    private val chatId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedMessageForAction = MutableStateFlow<Message?>(null)
    val selectedMessageForAction: StateFlow<Message?> = _selectedMessageForAction.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMessages(chatId).collect {
                _messages.value = it
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            repository.sendMessage(chatId, text, MessageType.TEXT)
            _inputText.value = ""
        }
    }

    fun sendVoiceMessage(durationSec: Int) {
        viewModelScope.launch {
            repository.sendMessage(chatId, "Voice message (${durationSec}s)", MessageType.VOICE)
        }
    }

    fun sendImage(url: String, caption: String = "") {
        viewModelScope.launch {
            repository.sendMessage(chatId, caption, MessageType.IMAGE, url)
        }
    }

    fun openMessageActions(message: Message) {
        _selectedMessageForAction.value = message
    }

    fun closeMessageActions() {
        _selectedMessageForAction.value = null
    }

    fun addReaction(emoji: String) {
        val message = _selectedMessageForAction.value ?: return
        viewModelScope.launch {
            repository.addReaction(message.id, emoji)
            closeMessageActions()
        }
    }

    fun setReminder(durationMinutes: Int) {
        val message = _selectedMessageForAction.value ?: return
        viewModelScope.launch {
            repository.setReminder(message.id, durationMinutes)
            closeMessageActions()
        }
    }

    fun togglePin() {
        val message = _selectedMessageForAction.value ?: return
        viewModelScope.launch {
            repository.togglePinMessage(message.id)
            closeMessageActions()
        }
    }

    fun toggleSave() {
        val message = _selectedMessageForAction.value ?: return
        viewModelScope.launch {
            repository.toggleSaveMessage(message.id)
            closeMessageActions()
        }
    }

    fun deleteMessage() {
        val message = _selectedMessageForAction.value ?: return
        viewModelScope.launch {
            repository.deleteMessage(message.id)
            closeMessageActions()
        }
    }
}
