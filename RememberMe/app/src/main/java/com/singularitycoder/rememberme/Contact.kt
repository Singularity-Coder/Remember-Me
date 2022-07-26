package com.singularitycoder.rememberme

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.singularitycoder.rememberme.helpers.TABLE_CONTACT
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = TABLE_CONTACT)
data class Contact(
    @ColumnInfo(name = "name") var name: String = "",
    @PrimaryKey @ColumnInfo(name = "mobileNumber") var mobileNumber: String = "",
    @ColumnInfo(name = "dateAdded") var dateAdded: Long = 0,
    @ColumnInfo(name = "imagePath") var imagePath: String = "",
    @ColumnInfo(name = "videoPath") var videoPath: String = "",
    @Ignore var photo: Bitmap? = null,
    @Ignore var photoURI: Uri? = null,
    @Ignore var isAlphabetShown: Boolean = false,
) : Parcelable {
    constructor() : this("", "", 0, "", "", null, null)
}