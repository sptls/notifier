package com.example.dupa

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling FetchWorker")

            val initialRequest = OneTimeWorkRequestBuilder<FetchWorker>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "web-fetch-loop",
                ExistingWorkPolicy.REPLACE,
                initialRequest
            )
        }
    }
}