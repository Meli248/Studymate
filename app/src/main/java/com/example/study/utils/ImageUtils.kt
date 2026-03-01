package com.example.study.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts

class ImageUtils(private val context: Context, private val registryOwner: ActivityResultRegistryOwner) {
    private lateinit var getContent: ActivityResultLauncher<String>

    fun registerLaunchers(callback: (Uri?) -> Unit) {
        getContent = registryOwner.activityResultRegistry.register(
            "image_picker",
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            callback(uri)
        }
    }

    fun launchImagePicker() {
        getContent.launch("image/*")
    }
}
