package com.singularitycoder.rememberme

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.rememberme.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Sync Contacts - Worker
// Sort list in alphabetic order
// Alphabet strip
// Custom square camera for group selfie videos for context, name, phone
// Data migration
// 10 sec videos
// Default to selfie camera
// Expanded card has video, name, phone, date added, call, message, whatsapp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                    // permission permanently denied. Show settings dialog
                    // enable blocking layout and show popup to go to settings
                    showPermissionSettings()
                }
                else -> {
                    // Permission denied but not permanently, tell user why you need it. Ideally provide a button to request it again and another to dismiss
                    // enable blocking layout
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

    private fun grantPermissions() {
        mainActivityPermissionsResult.launch(mainActivityPermissions)
    }

    private fun ActivityMainBinding.setupUI() {
        rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ContactsAdapter()
        }
        // TODO add loader
        CoroutineScope(IO).launch {
            val contactsList = getContacts()
            withContext(Main) {
                (rvContacts.adapter as ContactsAdapter).apply {
                    this.contactsList = contactsList
                    notifyDataSetChanged()
                }
            }
        }
    }
}