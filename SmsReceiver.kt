package com.example.axsmsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.provider.Telephony
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

        val messages = try { Telephony.Sms.Intents.getMessagesFromIntent(intent) } catch (_: Exception) { null } ?: return
        if (messages.isEmpty()) return
        val originating = messages.first().originatingAddress.orEmpty()
        if (normalize(originating) != expected) return
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        val now = System.currentTimeMillis()
        val lastHash = prefs.getString("last_hash", null)
        val hash = "$originating|$body|${now / 120000}"
        if (hash == lastHash) return

        val pending = goAsync()
        Thread {
            try {
                val manager = if (android.os.Build.VERSION.SDK_INT >= 31) context.getSystemService(SmsManager::class.java)
                              else @Suppress("DEPRECATION") SmsManager.getDefault()
                val parts = manager.divideMessage("Forwarded from $originating:\n$body")
                manager.sendMultipartTextMessage(destination, null, parts, null, null)
                appendLog(prefs, "SUCCESS  ${timestamp()}  From: $originating  To: $destination")
                prefs.edit().putString("last_hash", hash).apply()
            } catch (e: Exception) {
                appendLog(prefs, "FAILED  ${timestamp()}  ${e.javaClass.simpleName}: ${e.message ?: "SMS send failed"}")
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun normalize(value: String) = value.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")
    private fun timestamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    private fun appendLog(prefs: android.content.SharedPreferences, line: String) {
        val old = prefs.getString("log", "").orEmpty()
        prefs.edit().putString("log", (line + "\n" + old).take(5000)).apply()
    }

    companion object { const val PREFS = "ax_sms_forwarder" }
}
