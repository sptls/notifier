package com.example.dupa

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import android.widget.Toast
import android.widget.TextView



class MainActivity : AppCompatActivity() {

    private lateinit var serverInput: EditText
    private lateinit var userInput: EditText
    private lateinit var passInput: EditText
    private lateinit var saveButton: Button
    private val prefsName = "webprefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateWorkerStatusLabel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        serverInput = findViewById(R.id.server_input)
        userInput = findViewById(R.id.user_input)
        passInput = findViewById(R.id.pass_input)
        saveButton = findViewById(R.id.save_button)

        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        serverInput.setText(prefs.getString("server", "http://www.sptls.online"))
        userInput.setText(prefs.getString("user", "sptls"))
        passInput.setText(prefs.getString("pass", "2137"))

        saveButton.setOnClickListener {
            val server = serverInput.text.toString().trim()
            val user = userInput.text.toString().trim()
            val pass = passInput.text.toString().trim()

            prefs.edit()
                .putString("server", server)
                .putString("user", user)
                .putString("pass", pass)
                .apply()

            Log.d("MainActivity", "Saved settings and starting worker")

            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork("web-fetch-loop")

            val initialRequest = OneTimeWorkRequestBuilder<FetchWorker>().build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork("web-fetch-loop", ExistingWorkPolicy.REPLACE, initialRequest)
        }

        val restartWorkerButton: Button = findViewById(R.id.restart_worker_button)

        restartWorkerButton.setOnClickListener {
            Log.d("MainActivity", "Manual restart of FetchWorker requested")
            WorkManager.getInstance(applicationContext).cancelUniqueWork("web-fetch-loop")

            val initialRequest = OneTimeWorkRequestBuilder<FetchWorker>().build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork("web-fetch-loop", ExistingWorkPolicy.REPLACE, initialRequest)
        }

        val stopWorkerButton: Button = findViewById(R.id.stop_worker_button)

        stopWorkerButton.setOnClickListener {
            Log.d("MainActivity", "Stopping background worker")

            // Cancel both fast loop and fallback periodic worker
            WorkManager.getInstance(applicationContext).cancelUniqueWork("web-fetch-loop")
            WorkManager.getInstance(applicationContext).cancelUniqueWork("web-fetch-persistent")

            Toast.makeText(this, "Notifications stopped", Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateWorkerStatusLabel() {
        val label: TextView = findViewById(R.id.worker_status_label)

        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData("web-fetch-loop")
            .observe(this) { infos ->
                val isActive = infos.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                }

                val statusText = if (isActive) "on" else "off"
                val fullText = "Notifier status: $statusText"
                val spannable = android.text.SpannableString(fullText)

                val color = if (isActive)
                    getColor(android.R.color.holo_green_dark)
                else
                    getColor(android.R.color.holo_red_dark)

                val start = fullText.indexOf(statusText)
                val end = start + statusText.length

                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(color),
                    start,
                    end,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                label.text = spannable
            }
    }

}
