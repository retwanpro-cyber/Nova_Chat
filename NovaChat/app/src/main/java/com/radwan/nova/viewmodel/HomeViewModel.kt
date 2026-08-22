package com.radwan.nova.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radwan.nova.data.models.Chat
import com.radwan.nova.data.repository.NovaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ChatFilterTab {
    ALL, UNREAD, GROUPS
}

class HomeViewModel(private val repository: NovaRepository) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _selectedTab = MutableStateFlow(ChatFilterTab.ALL)
    val selectedTab: StateFlow<ChatFilterTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getChats().collect { chatList ->
                _chats.value = chatList
            }
        }
    }

    fun setFilterTab(tab: ChatFilterTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
