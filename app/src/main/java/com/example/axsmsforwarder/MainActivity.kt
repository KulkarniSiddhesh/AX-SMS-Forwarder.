package com.example.axsmsforwarder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(SmsReceiver.PREFS, MODE_PRIVATE)

        setContent {
            MaterialTheme {
                var sender by remember {
                    mutableStateOf(prefs.getString("sender", "AX-FNTESA-S") ?: "AX-FNTESA-S")
                }
                var destination by remember {
                    mutableStateOf(prefs.getString("destination", "") ?: "")
                }
                var enabled by remember {
                    mutableStateOf(prefs.getBoolean("enabled", false))
                }
                var log by remember {
                    mutableStateOf(prefs.getString("log", "") ?: "")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("AX SMS Forwarder", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Forward only incoming SMS messages from the sender ID you specify. " +
                            "Messages are sent as SMS to your chosen destination. Carrier charges may apply."
                    )

                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it },
                        label = { Text("Allowed sender ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            prefs.edit()
                                .putString("sender", sender.trim())
                                .putString("destination", destination.trim())
                                .apply()

                            val needed = arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.SEND_SMS
                            )
                            if (needed.any {
                                    ContextCompat.checkSelfPermission(
                                        this@MainActivity, it
                                    ) != PackageManager.PERMISSION_GRANTED
                                }
                            ) {
                                permissionLauncher.launch(needed)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save settings and grant permissions")
                    }

                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Forwarding enabled", modifier = Modifier.weight(1f))
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                prefs.edit().putBoolean("enabled", it).apply()
                            }
                        )
                    }

                    Text("Keep forwarding off until the sender and destination are correct and permissions are granted.")

                    Button(
                        onClick = { log = prefs.getString("log", "") ?: "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Refresh history")
                    }

                    Text("Recent activity", style = MaterialTheme.typography.titleMedium)
                    Text(if (log.isBlank()) "No forwarding activity yet." else log)

                    Text(
                        "Privacy: sender ID, destination and log are stored locally on this device. " +
                            "No server or analytics are used. This app requires SMS permissions and may incur SMS charges. " +
                            "Do not forward OTPs or sensitive messages to a number you do not control."
                    )
                }
            }
        }
    }
}
