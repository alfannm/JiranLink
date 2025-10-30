package com.aidea.jiranlink.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.agconnect.cloud.storage.core.StorageReference
import com.huawei.agconnect.cloud.storage.core.UploadTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AuthViewModel : ViewModel() {

    private val _user = MutableStateFlow<AGConnectUser?>(null)
    val user: StateFlow<AGConnectUser?> = _user

    private val _lastUploadUrl = MutableStateFlow<String?>(null)
    val lastUploadUrl: StateFlow<String?> = _lastUploadUrl

    fun loadCurrentUser() {
        viewModelScope.launch {
            _user.value = AGConnectAuth.getInstance().currentUser
        }
    }

    fun signOut() {
        viewModelScope.launch {
            AGConnectAuth.getInstance().signOut()
            _user.value = null
            _lastUploadUrl.value = null
        }
    }

    fun uploadImageToCloudStorage(context: Context, fileUri: Uri) {
        val current = AGConnectAuth.getInstance().currentUser
        val uid = current?.uid ?: "anonymous"
        val fileName = "${UUID.randomUUID()}.jpg"
        val path = "users/$uid/images/$fileName"

        val storage = AGCStorageManagement.getInstance()
        val ref: StorageReference = storage.getStorageReference(path)

        val file = uriToFile(context, fileUri)
        if (file == null) {
            _lastUploadUrl.value = "Invalid file Uri"
            return
        }

        val task: UploadTask = ref.putFile(file)
        task.addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                _lastUploadUrl.value = url.toString()
            }.addOnFailureListener { e ->
                _lastUploadUrl.value = "Error getting URL: ${e.message}"
            }
        }.addOnFailureListener { e ->
            _lastUploadUrl.value = "Upload failed: ${e.message}"
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
