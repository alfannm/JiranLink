package com.aidea.jiranlink.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    user: String,
    lastUploadUrl: String?,
    onPickImage: (Uri) -> Unit,
    onSignOut: () -> Unit
) {
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPickImage(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $user 👋",
            style = MaterialTheme.typography.titleLarge
        )

        if (lastUploadUrl != null) {
            Text(
                text = "Last uploaded URL:\n$lastUploadUrl",
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }

        Button(onClick = {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Upload an image to Cloud Storage")
        }

        OutlinedButton(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}
