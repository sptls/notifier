package com.example.notifier

import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class FetchWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val channelId = "web_fetch_channel_v"
    private val prefsName = "webprefs"
    private val counterKey = "lastSeenId"
    private val user = "sptls"
    private val pass = "2137"

    override suspend fun doWork(): Result {
        Log.d("FetchWorker", "Worker started")

        val lastSeenId = getLocalCounter()
        val latestServerId = getLatestIdFromServer()

        Log.d("FetchWorker", "Local lastSeenId=$lastSeenId, Server latestId=$latestServerId")

        if (latestServerId == null || latestServerId <= lastSeenId) {
            Log.d("FetchWorker", "No new messages.")
            scheduleNextRun()
            return Result.success()
        }

        for (id in (lastSeenId + 1)..latestServerId) {
            val message = getMessageForId(id)
            if (message != null) {
                val (title, body) = message
                showNotification(title, body)
                Log.d("FetchWorker", "Notification for ID $id shown")
                saveLocalCounter(id)
            } else {
                Log.w("FetchWorker", "Invalid or empty message for ID $id")
            }
        }

        scheduleNextRun()
        return Result.success()
    }

    private fun getLocalCounter(): Int {
        val prefs = applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return prefs.getInt(counterKey, -1)
    }

    private fun saveLocalCounter(id: Int) {
        val prefs = applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putInt(counterKey, id).apply()
    }

    private suspend fun getLatestIdFromServer(): Int? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val formBody = FormBody.Builder()
                .add("user", user)
                .build()
            val request = Request.Builder()
                .url("http://www.sptls.online/getcounter.php")
                .post(formBody)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            Log.d("FetchWorker", "Server returned counter: $body")
            body?.toIntOrNull()
        } catch (e: Exception) {
            Log.e("FetchWorker", "Error fetching latest counter", e)
            null
        }
    }

    private suspend fun getMessageForId(id: Int): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val formBody = FormBody.Builder()
                .add("user", user)
                .add("pass", pass)
                .add("id", id.toString())
                .build()
            val request = Request.Builder()
                .url("http://www.sptls.online/getmsg.php")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()?.trim()
            Log.d("FetchWorker", "Raw message for ID $id:\n$body")

            val lines = body?.lines()
            if (lines != null && lines.size >= 2) {
                Pair(lines[0].trim(), lines[1].trim())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FetchWorker", "Error fetching message for ID $id", e)
            null
        }
    }

    private fun showNotification(title: String, text: String) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("body", text)
            putExtra("open_detail", true)
        }


        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text) // full message shown in popup
            .setStyle(NotificationCompat.BigTextStyle().bigText(text)) // full message in expanded view
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            System.currentTimeMillis().toInt(),
            notification
        )

        NotificationStorage.addEntry(applicationContext, NotificationLogEntry(title, text, System.currentTimeMillis()))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Web Fetch Channel"
            val descriptionText = "Notifications from sptls.online"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNextRun() {
        val next = OneTimeWorkRequestBuilder<FetchWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .addTag("web-fetch-loop")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "web-fetch-loop",
                ExistingWorkPolicy.REPLACE,
                next
            )

        Log.d("FetchWorker", "Scheduled next check in 15 seconds")
    }
}
