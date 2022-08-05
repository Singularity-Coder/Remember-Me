package com.singularitycoder.rememberme

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ContactDao {

    // Single Item CRUD ops ------------------------------------------------------------------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Transaction
    @Query("SELECT * FROM $TABLE_CONTACT WHERE mobileNumber LIKE :mobileNumber LIMIT 1")
    suspend fun getContactByPhone(mobileNumber: String): Contact?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    // ---------------------------------------------------------------------------------------------------------------------------------------------

    // All of the parameters of the Insert method must either be classes annotated with Entity or collections/array of it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contactList: List<Contact>)

    @Query("SELECT * FROM $TABLE_CONTACT")
    fun getAllContactsListLiveData(): LiveData<List<Contact>>

    @Query("SELECT * FROM $TABLE_CONTACT")
    fun getLatestContactLiveData(): LiveData<Contact>

    @Query("SELECT * FROM $TABLE_CONTACT")
    suspend fun getAll(): List<Contact>

    @Query("DELETE FROM $TABLE_CONTACT")
    suspend fun deleteAll()
}
