package com.litechat.app.ui.screens.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.litechat.app.core.di.ServiceLocator
import com.litechat.app.data.db.entity.ContactEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val contactRepo = ServiceLocator.provideContactRepository(application)
    private val conversationRepo = ServiceLocator.provideConversationRepository(application)

    val contacts: StateFlow<List<ContactEntity>> = contactRepo.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun startChatWith(contact: ContactEntity, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            val conversation = conversationRepo.getOrCreateConversation(
                peerId = contact.id,
                peerName = contact.displayName
            )
            onChatCreated(conversation.id)
        }
    }

    fun addContact(contact: ContactEntity) {
        viewModelScope.launch {
            contactRepo.upsertContact(contact)
        }
    }
}
