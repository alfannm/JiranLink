package com.aidea.jiranlink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.service.AccountAuthService


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
    val context = LocalContext.current
    var status by remember { mutableStateOf("Not signed in") }

    // Launcher to handle sign-in result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(result.data)
            if (authHuaweiIdTask.isSuccessful) {
                val huaweiId: AuthHuaweiId = authHuaweiIdTask.result
                val authCode = huaweiId.authorizationCode // safer replacement for accessToken

                // Sign in with AGConnect using Huawei ID
                val credential = HwIdAuthProvider.credentialWithToken(authCode)
                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener {
                        val user: AGConnectUser? = it.user
                        status = "Signed in as: ${user?.email ?: user?.uid}"
                    }
                    .addOnFailureListener { e ->
                        status = "Sign-in failed: ${e.message}"
                    }
            } else {
                status = "Auth failed: ${authHuaweiIdTask.exception?.message}"
            }
        } else {
            status = "Cancelled or failed"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            // Request Huawei Account scope (base profile info)
            val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
            .createParams()
            val service: HuaweiIdAuthService = HuaweiIdAuthManager.getService(context, authParams)
            launcher.launch(service.signInIntent)
        }) {
            Text("Sign in with Huawei ID")
        }
    }
}
