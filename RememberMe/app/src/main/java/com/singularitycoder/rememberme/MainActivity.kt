package com.singularitycoder.rememberme

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
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

    private val contactsAdapter = ContactsAdapter()
    private val mainActivityPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, @JvmSuppressWildcards Boolean>? ->
        permissions ?: return@registerForActivityResult
        permissions.entries.forEach { it: Map.Entry<String, @JvmSuppressWildcards Boolean> ->
            println("Permission status: ${it.key} = ${it.value}")
            val permission = it.key
            val isGranted = it.value
            when {
                isGranted -> {
                    // disable blocking layout and proceed
                    binding.setupUI()
                    binding.setupUserActionListeners()
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
        grantPermissions()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_VIDEO) return
        if (resultCode != Activity.RESULT_OK) return
        data ?: return
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
        CoroutineScope(IO).launch {
            getContacts().sortedBy { it.name }.forEach { it: Contact ->
                dao.insert(it)
            }
            withContext(Main) {
                contactsAdapter.apply {
                    this.contactsList = dao.getAll()
                    notifyDataSetChanged()
                    progressCircular.isVisible = false
                }
            }
        }
    }

    private fun ActivityMainBinding.setupUserActionListeners() {
        contactsAdapter.setItemClickListener {

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
        rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10) fabAddContact.hide() else if (dy < 10) fabAddContact.show()
            }
        })
    }
}