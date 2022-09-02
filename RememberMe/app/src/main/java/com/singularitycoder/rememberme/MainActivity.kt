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
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var isRetake = false
    private var retakeContact: Contact? = null

    private val duplicateContactsList = mutableListOf<Contact>()
    private val contactsAdapter = ContactsAdapter()
    private val contactsPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            showPermissionSettings()
            return@registerForActivityResult
        }
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
            duplicateContactsList.addAll(sortedContactList)

            withContext(Main) {
                contactsAdapter.notifyDataSetChanged()
                if (contactsAdapter.contactsList.size < 5) binding.tvAlphabet.isVisible = false
                binding.progressCircular.isVisible = false
            }
        }
    }
    private val cameraPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted: Boolean? ->
        isPermissionGranted ?: return@registerForActivityResult
        if (isPermissionGranted.not()) {
            showPermissionSettings()
            return@registerForActivityResult
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onResume() {
        super.onResume()
        grantContactsPermissions()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_VIDEO) return
        if (resultCode != Activity.RESULT_OK) return
        data ?: return
        val thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(takenVideoFile.path, MediaStore.Images.Thumbnails.MINI_KIND)
        println("originalVideoPath: ${takenVideoFile.absolutePath}")
        AddContactBottomSheetFragment.newInstance(
            contact = if (isRetake) retakeContact.apply { this?.videoPath = takenVideoFile.absolutePath } else Contact(videoPath = takenVideoFile.absolutePath),
            userAction = if (isRetake) UserAction.UPDATE else UserAction.ADD
        ).show(supportFragmentManager, TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET)
        isRetake = false
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun ActivityMainBinding.setupUI() {
        linearLayoutManager = LinearLayoutManager(this@MainActivity)
        rvContacts.apply {
            layoutManager = linearLayoutManager
            adapter = contactsAdapter
        }
        progressCircular.isVisible = true
    }

    private fun ActivityMainBinding.setupUserActionListeners() {
        etSearch.doAfterTextChanged { keyWord: Editable? ->
            ibClearSearch.isVisible = keyWord.isNullOrBlank().not()
            tvAlphabet.isVisible = keyWord.isNullOrBlank()
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
        contactsAdapter.setImageClickListener { it: Contact ->
            if (it.videoPath.isBlank()) return@setImageClickListener
            VideoBottomSheetFragment.newInstance(videoPath = it.videoPath)
                .show(supportFragmentManager, TAG_VIDEO_MODAL_BOTTOM_SHEET)
        }
        contactsAdapter.setEditContactClickListener { it: Contact ->
            AddContactBottomSheetFragment.newInstance(contact = it, userAction = UserAction.UPDATE)
                .show(supportFragmentManager, TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET)
        }
        fabAddContact.setOnClickListener {
            if (isCameraPresent().not()) {
                Snackbar.make(binding.root, "You don't have a camera on your device!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            grantCameraPermissions()
        }
        // https://stackoverflow.com/questions/32038332/using-google-design-library-how-to-hide-fab-button-on-scroll-down
        rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10) fabAddContact.hide() else if (dy < 10) fabAddContact.show()
                if (contactsAdapter.contactsList.isNotEmpty()) {
                    tvAlphabet.text = contactsAdapter.contactsList.get(linearLayoutManager.findFirstVisibleItemPosition()).name.substring(0, 1)
                }
            }
        })
    }

    private fun grantContactsPermissions() {
        contactsPermissionResult.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun grantCameraPermissions() {
        cameraPermissionResult.launch(Manifest.permission.CAMERA)
    }

    fun retakeVideo(contact: Contact?) {
        isRetake = true
        retakeContact = contact
        binding.fabAddContact.performClick()
    }

    fun addContact(contact: Contact) {
        CoroutineScope(IO).launch {
            dao.insert(contact)
            withContext(Main) {
                grantContactsPermissions()
            }
        }
    }

    fun updateContact(contact: Contact) {
        CoroutineScope(IO).launch {
            dao.update(contact)
            withContext(Main) {
                grantContactsPermissions()
            }
        }
    }
}