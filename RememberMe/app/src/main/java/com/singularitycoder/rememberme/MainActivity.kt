package com.singularitycoder.rememberme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.singularitycoder.rememberme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupUI()
    }

    private fun ActivityMainBinding.setupUI() {

    }
}