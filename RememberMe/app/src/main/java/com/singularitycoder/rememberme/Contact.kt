package com.singularitycoder.rememberme

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = TABLE_CONTACT)
data class Contact(
    @PrimaryKey @ColumnInfo(name = "id") var id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "mobileNumber") var mobileNumber: String,
    @ColumnInfo(name = "dateAdded") var dateAdded: Long,
    @Ignore var photo: Bitmap? = null,
    @Ignore var photoURI: Uri? = null,
) {
    constructor() : this("", "", "", 0,null, null)
}