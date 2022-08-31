package com.singularitycoder.rememberme

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = TABLE_CONTACT)
data class Contact(
    @ColumnInfo(name = "name") var name: String,
    @PrimaryKey @ColumnInfo(name = "mobileNumber") var mobileNumber: String,
    @ColumnInfo(name = "dateAdded") var dateAdded: Long,
    @ColumnInfo(name = "imagePath") var imagePath: String,
    @Ignore var photo: Bitmap? = null,
    @Ignore var photoURI: Uri? = null,
    @Ignore var isAlphabetShown: Boolean = false,
) {
    constructor() : this("", "", 0, "", null, null)
}