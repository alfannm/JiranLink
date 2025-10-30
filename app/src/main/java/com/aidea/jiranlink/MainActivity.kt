package com.aidea.jiranlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aidea.jiranlink.viewmodel.AuthViewModel
import com.aidea.jiranlink.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authVm = remember { AuthViewModel() }
            val context = LocalContext.current
            val user by authVm.user.collectAsState()
            val lastUrl by authVm.lastUploadUrl.collectAsState()

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (user != null) {
                        HomeScreen(
                            user = user!!,
                            lastUploadUrl = lastUrl,
                            onPickImage = { uri -> authVm.uploadImageToCloudStorage(context, uri) },
                            onSignOut = { authVm.signOut() }
                        )
                    } else {
                        LoginScreen(authVm)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(authVm: AuthViewModel) {
    val status = remember { mutableStateOf("Not signed in") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status.value)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            status.value = "Mock login successful."
            authVm.mockSignIn()
        }) {
            Text("Mock Login (for Emulator)")
        }
    }
}
