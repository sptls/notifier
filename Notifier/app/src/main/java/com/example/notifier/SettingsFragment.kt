package com.example.notifier

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.work.*

class SettingsFragment : Fragment() {

    private val prefsName = "webprefs"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val serverInput: EditText = view.findViewById(R.id.server_input)
        val userInput: EditText = view.findViewById(R.id.user_input)
        val passInput: EditText = view.findViewById(R.id.pass_input)
        val saveButton: Button = view.findViewById(R.id.save_button)
        val restartButton: Button = view.findViewById(R.id.restart_worker_button)
        val stopButton: Button = view.findViewById(R.id.stop_worker_button)
        val statusLabel: TextView = view.findViewById(R.id.worker_status_label)

        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        serverInput.setText(prefs.getString("server", "http://www.sptls.online"))
        userInput.setText(prefs.getString("user", "sptls"))
        passInput.setText(prefs.getString("pass", "2137"))

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("server", serverInput.text.toString().trim())
                .putString("user", userInput.text.toString().trim())
                .putString("pass", passInput.text.toString().trim())
                .apply()

            Log.d("SettingsFragment", "Saved settings and started worker")

            WorkManager.getInstance(requireContext())
                .cancelUniqueWork("web-fetch-loop")

            val initialRequest = OneTimeWorkRequestBuilder<FetchWorker>().build()

            WorkManager.getInstance(requireContext())
                .enqueueUniqueWork("web-fetch-loop", ExistingWorkPolicy.REPLACE, initialRequest)
        }

        restartButton.setOnClickListener {
            WorkManager.getInstance(requireContext())
                .cancelUniqueWork("web-fetch-loop")

            val request = OneTimeWorkRequestBuilder<FetchWorker>().build()
            WorkManager.getInstance(requireContext())
                .enqueueUniqueWork("web-fetch-loop", ExistingWorkPolicy.REPLACE, request)
        }

        stopButton.setOnClickListener {
            WorkManager.getInstance(requireContext()).cancelUniqueWork("web-fetch-loop")
            WorkManager.getInstance(requireContext()).cancelUniqueWork("web-fetch-persistent")
            Toast.makeText(requireContext(), "Notifications stopped", Toast.LENGTH_SHORT).show()
        }

        val resetButton: Button = view.findViewById(R.id.reset_button)

        resetButton.setOnClickListener {
            val user = userInput.text.toString().trim()
            val pass = passInput.text.toString().trim()
            val server = serverInput.text.toString().trim()

            Thread {
                try {
                    val client = okhttp3.OkHttpClient()
                    val formBody = okhttp3.FormBody.Builder()
                        .add("user", user)
                        .add("pass", pass)
                        .build()

                    val request = okhttp3.Request.Builder()
                        .url("$server/reset.php")
                        .post(formBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseText = response.body?.string()?.trim()

                    if (response.isSuccessful) {
                        // Reset local counter
                        requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                            .edit().putInt("lastSeenId", -1).apply()

                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Reset successful: $responseText", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Reset failed: $responseText", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Reset error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }

        // Update status label
        WorkManager.getInstance(requireContext())
            .getWorkInfosForUniqueWorkLiveData("web-fetch-loop")
            .observe(viewLifecycleOwner) { infos ->
                val isActive = infos.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                }

                val statusText = if (isActive) "on" else "off"
                val fullText = "Notifier status: $statusText"
                val spannable = android.text.SpannableString(fullText)

                val color = if (isActive)
                    requireContext().getColor(android.R.color.holo_green_dark)
                else
                    requireContext().getColor(android.R.color.holo_red_dark)

                val start = fullText.indexOf(statusText)
                val end = start + statusText.length

                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(color),
                    start,
                    end,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                statusLabel.text = spannable
            }

        return view
    }
}
