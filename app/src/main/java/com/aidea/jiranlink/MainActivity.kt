package com.aidea.jiranlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.SignInResult
import com.huawei.agconnect.auth.HuaweiIdAuthProvider
import com.huawei.agconnect.auth.AGConnectAuthException
import com.huawei.agconnect.auth.AGConnectUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var status by remember { mutableStateOf("Not signed in") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            try {
                val credential = HuaweiIdAuthProvider.credentialWithToken("<token>")
                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener { signInResult: SignInResult ->
                        val user: AGConnectUser? = signInResult.user
                        status = "Signed in as: ${user?.uid}"
                    }
                    .addOnFailureListener { e: Exception ->
                        if (e is AGConnectAuthException) {
                            status = "Error: ${e.message}"
                        } else {
                            status = "Sign-in failed"
                        }
                    }
            } catch (e: Exception) {
                status = "Exception: ${e.message}"
            }
        }) {
            Text("Sign in with Huawei ID")
        }
    }
}
