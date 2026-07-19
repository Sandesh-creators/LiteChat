package com.litechat.app.data.repository

import com.litechat.app.data.db.dao.ContactDao
import com.litechat.app.data.db.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    fun getAllContacts(): Flow<List<ContactEntity>> =
        contactDao.getAllContacts()

    fun getRegisteredContacts(): Flow<List<ContactEntity>> =
        contactDao.getRegisteredContacts()

    fun searchContacts(query: String): Flow<List<ContactEntity>> =
        contactDao.searchContacts(query)

    suspend fun getContactById(id: String): ContactEntity? =
        contactDao.getContactById(id)

    suspend fun upsertContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun updateOnlineStatus(contactId: String, isOnline: Boolean) {
        contactDao.updateOnlineStatus(contactId, isOnline, System.currentTimeMillis())
    }
}
