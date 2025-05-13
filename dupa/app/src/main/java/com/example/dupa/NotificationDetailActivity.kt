package com.example.dupa

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        val title = intent.getStringExtra("title") ?: "No Title"
        val body = intent.getStringExtra("body") ?: "No Content"

        val titleView: TextView = findViewById(R.id.detail_title)
        val bodyView: TextView = findViewById(R.id.detail_body)

        titleView.text = title
        bodyView.text = body
    }
}