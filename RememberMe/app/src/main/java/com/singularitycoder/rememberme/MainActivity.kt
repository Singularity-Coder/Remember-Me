package com.singularitycoder.rememberme

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.singularitycoder.rememberme.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// Sync Contacts - Worker
// Alphabet strip
// Custom square camera for group selfie videos for context, name, phone, context field just in case u r not able to video it
// Data migration
// Expanded card has video, name, phone, date added, call, message, whatsapp
// Get data from viewmodel
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dao: ContactDao

    private lateinit var binding: ActivityMainBinding
    private lateinit var takenVideoFile: File

    private val duplicateContactsList = mutableListOf<Contact>()
    private val contactsAdapter = ContactsAdapter()
    private val mainActivityPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
        permissions ?: return@registerForActivityResult
        val isAllPermissionsGranted = permissions.entries.all { it.value }
        permissions.entries.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            println("Permission status: ${it.key} = ${it.value}")
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> {
                    // disable blocking layout and proceed
                    if (permission != Manifest.permission.READ_CONTACTS) return@forEach
                    CoroutineScope(IO).launch {
                        getContacts().sortedBy { it.name }.forEach { it: Contact ->
                            dao.insert(it)
                        }
                        val sortedContactList = ArrayList<Contact>()
                        val contactsMap = HashMap<String, ArrayList<Contact>>()
                        dao.getAll().forEach { it: Contact ->
                            contactsMap.put(
                                it.name.subSequence(0, 1).toString(),
                                contactsMap.get(it.name.subSequence(0, 1).toString())?.apply {
                                    add(it)
                                } ?: ArrayList<Contact>().apply {
                                    add(it)
                                }
                            )
                        }
                        contactsMap.keys.sorted().forEach { it: String ->
                            val preparedList = contactsMap.get(it)?.mapIndexed { index, contact ->
                                if (index == 0) contact.isAlphabetShown = true
                                contact
                            } ?: emptyList()
                            sortedContactList.addAll(preparedList)
                        }
                        contactsAdapter.contactsList = sortedContactList
                        duplicateContactsList.clear()
                        duplicateContactsList.addAll(dao.getAll())

                        withContext(Main) {
                            contactsAdapter.notifyDataSetChanged()
                            binding.progressCircular.isVisible = false
                        }
                    }
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                    // permission permanently denied. Show settings dialog enable blocking layout and show popup to go to settings
                    showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss enable blocking layout
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        grantPermissions()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_VIDEO) return
        if (resultCode != Activity.RESULT_OK) return
        data ?: return
        val thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(takenVideoFile.path, MediaStore.Images.Thumbnails.MINI_KIND)
        println("originalVideoPath: ${takenVideoFile.absolutePath}")

        // TODO Launch Bottom sheet
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun grantPermissions() {
        mainActivityPermissionsResult.launch(mainActivityPermissions)
    }

    private fun ActivityMainBinding.setupUI() {
        rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactsAdapter
        }
        progressCircular.isVisible = true
    }

    private fun ActivityMainBinding.setupUserActionListeners() {
        etSearch.doAfterTextChanged { keyWord: Editable? ->
            ibClearSearch.isVisible = keyWord.isNullOrBlank().not()
            if (keyWord.isNullOrBlank()) {
                contactsAdapter.contactsList = duplicateContactsList
            } else {
                contactsAdapter.contactsList = contactsAdapter.contactsList.filter { it: Contact -> it.name.contains(keyWord) }
            }
            contactsAdapter.notifyDataSetChanged()
        }
        ibClearSearch.setOnClickListener {
            etSearch.setText("")
        }
        contactsAdapter.setItemClickListener { it: Contact ->
        }
        contactsAdapter.setEditContactClickListener { it: Contact ->
        }
        fabAddContact.setOnClickListener {
            if (isCameraPresent().not()) {
                Snackbar.make(binding.root, "You don't have a camera on your device!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            takenVideoFile = internalFilesDir(fileName = "camera_video_${System.currentTimeMillis()}.mp4").also {
                if (!it.exists()) it.createNewFile()
            }
            /**
             * fileProvider file should be exactly in the "path" attribute that u define in file_paths.xml and declare provider in manifest
             * https://developer.android.com/reference/android/provider/MediaStore#EXTRA_DURATION_LIMIT
             * */
            val fileProvider = FileProvider.getUriForFile(this@MainActivity, FILE_PROVIDER_AUTHORITY, takenVideoFile)
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
                putExtra("android.intent.extras.CAMERA_FACING", 1) // Front facing by default
                putExtra("android.intent.extra.durationLimit", 15) // Max 15 sec video
            }
            startActivityForResult(intent, REQUEST_CODE_VIDEO)
            doAfter(3000L) {
                showToast("Why, What, When, Where and How you met this person?")
            }
        }
        // https://stackoverflow.com/questions/32038332/using-google-design-library-how-to-hide-fab-button-on-scroll-down
        rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10) fabAddContact.hide() else if (dy < 10) fabAddContact.show()
            }
        })
    }
}