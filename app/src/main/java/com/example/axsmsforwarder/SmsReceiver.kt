package com.example.axsmsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return

        val expected = normalize(prefs.getString("sender", "AX-FNTESA-S") ?: "")
        val destination = prefs.getString("destination", "")?.trim().orEmpty()
        if (expected.isBlank() || destination.isBlank()) return

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (_: Exception) {
            null
        } ?: return

        if (messages.isEmpty()) return

        val originating = messages.first().originatingAddress.orEmpty()
        if (normalize(originating) != expected) return

        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        val pending = goAsync()
        Thread {
            try {
                val manager = getSmsManager(context)
                val parts = manager.divideMessage("Forwarded from $originating:\n$body")
                manager.sendMultipartTextMessage(destination, null, parts, null, null)
                appendLog(
                    prefs,
                    "SUCCESS  ${timestamp()}  From: $originating  To: $destination"
                )
            } catch (e: Exception) {
                appendLog(
                    prefs,
                    "FAILED  ${timestamp()}  ${e.javaClass.simpleName}: ${e.message ?: "SMS send failed"}"
                )
            } finally {
                pending.finish()
            }
        }.start()
    }

    @Suppress("DEPRECATION")
    private fun getSmsManager(context: Context): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
    }

    private fun normalize(value: String): String =
        value.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")

    private fun timestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    private fun appendLog(prefs: android.content.SharedPreferences, line: String) {
        val old = prefs.getString("log", "").orEmpty()
        prefs.edit().putString("log", (line + "\n" + old).take(5000)).apply()
    }

    companion object {
        const val PREFS = "ax_sms_forwarder"
    }
}
