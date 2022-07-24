package com.singularitycoder.rememberme

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

const val DB_AUDIO_WEB = "db_contact"
const val TABLE_WEB_PAGE = "table_contact"

fun View.showSnackBar(
    message: String,
    anchorView: View? = null,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionBtnText: String = "NA",
    action: () -> Unit = {},
) {
    Snackbar.make(this, message, duration).apply {
        this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        if (null != anchorView) this.anchorView = anchorView
        if ("NA" != actionBtnText) setAction(actionBtnText) { action.invoke() }
        this.show()
    }
}

fun getDeviceSize(): Point = try {
    Point(deviceWidth(), deviceHeight())
} catch (e: Exception) {
    e.printStackTrace()
    Point(0, 0)
}

fun deviceWidth() = Resources.getSystem().displayMetrics.widthPixels

fun deviceHeight() = Resources.getSystem().displayMetrics.heightPixels

fun File?.customPath(directory: String?, fileName: String?): String {
    var path = this?.absolutePath

    if (directory != null) {
        path += File.separator + directory
    }

    if (fileName != null) {
        path += File.separator + fileName
    }

    return path ?: ""
}

/** /data/user/0/com.singularitycoder.audioweb/files */
fun Context.internalFilesDir(
    directory: String? = null,
    fileName: String? = null,
): File = File(filesDir.customPath(directory, fileName))

/** /storage/emulated/0/Android/data/com.singularitycoder.audioweb/files */
fun Context.externalFilesDir(
    rootDir: String = "",
    subDir: String? = null,
    fileName: String? = null,
): File = File(getExternalFilesDir(rootDir).customPath(subDir, fileName))

inline fun deleteAllFilesFrom(
    directory: File?,
    withName: String,
    crossinline onDone: () -> Unit = {},
) {
    CoroutineScope(Default).launch {
        directory?.listFiles()?.forEach files@{ it: File? ->
            it ?: return@files
            if (it.name.contains(withName)) {
                if (it.exists()) it.delete()
            }
        }

        withContext(Main) { onDone.invoke() }
    }
}


// Get Epoch Time
val timeNow: Long
    get() = System.currentTimeMillis()

fun Long.toIntuitiveDateTime(): String {
    val postedTime = this
    val elapsedTimeMillis = timeNow - postedTime
    val elapsedTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis)
    val elapsedTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis)
    val elapsedTimeInHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
    val elapsedTimeInDays = TimeUnit.MILLISECONDS.toDays(elapsedTimeMillis)
    val elapsedTimeInMonths = elapsedTimeInDays / 30
    return when {
        elapsedTimeInSeconds < 60 -> "Now"
        elapsedTimeInMinutes == 1L -> "$elapsedTimeInMinutes Minute ago"
        elapsedTimeInMinutes < 60 -> "$elapsedTimeInMinutes Minutes ago"
        elapsedTimeInHours == 1L -> "$elapsedTimeInHours Hour ago"
        elapsedTimeInHours < 24 -> "$elapsedTimeInHours Hours ago"
        elapsedTimeInDays == 1L -> "$elapsedTimeInDays Day ago"
        elapsedTimeInDays < 30 -> "$elapsedTimeInDays Days ago"
        elapsedTimeInMonths == 1L -> "$elapsedTimeInMonths Month ago"
        elapsedTimeInMonths < 12 -> "$elapsedTimeInMonths Months ago"
        else -> postedTime toTimeOfType DateType.dd_MMM_yyyy_hh_mm_a
    }
}

infix fun Long.toTimeOfType(type: DateType): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(type.value, Locale.getDefault())
    return dateFormat.format(date)
}

val mainActivityPermissions = arrayOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.WRITE_CONTACTS,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO
)

fun Context.hasContactPermission(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED

// https://stackoverflow.com/questions/12562151/android-get-all-contacts
fun Context.getContacts(): List<Contact> {
    val list: MutableList<Contact> = ArrayList()
    val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
    if ((cursor?.count ?: 0) > 0) {
        while (cursor?.moveToNext() == true) {
            val id: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID) ?: 0)
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) ?: 0) > 0) {
                val cursorInfo: Cursor? = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                )
                val inputStream: InputStream? = ContactsContract.Contacts.openContactPhotoInputStream(
                    contentResolver,
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                )
                val person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                val photoURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
                while (cursorInfo?.moveToNext() == true) {
                    val info = Contact().apply {
                        this.id = id
                        this.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) ?: 0)
                        this.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) ?: 0)
                        this.photo = if (inputStream != null) BitmapFactory.decodeStream(inputStream) else null
                        this.photoURI = photoURI
                    }
                    list.add(info)
                }
                cursorInfo?.close()
            }
        }
        cursor?.close()
    }
    return list
}

enum class DateType(val value: String) {
    dd_MMM_yyyy(value = "dd MMM yyyy"),
    dd_MMM_yyyy_h_mm_a(value = "dd-MMM-yyyy h:mm a"),
    dd_MMM_yyyy_hh_mm_a(value = "dd MMM yyyy, hh:mm a"),
    dd_MMM_yyyy_hh_mm_ss_a(value = "dd MMM yyyy, hh:mm:ss a"),
    dd_MMM_yyyy_h_mm_ss_aaa(value = "dd MMM yyyy, h:mm:ss aaa"),
    yyyy_MM_dd_T_HH_mm_ss_SS_Z(value = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
}
