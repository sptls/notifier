package com.example.notifier

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object NotificationStorage {
    private const val fileName = "notification_log.json"

    fun addEntry(context: Context, entry: NotificationLogEntry) {
        val entries = getAllEntries(context).toMutableList()
        entries.add(0, entry) // latest at top
        val json = Gson().toJson(entries)
        File(context.filesDir, fileName).writeText(json)
    }

    fun getAllEntries(context: Context): List<NotificationLogEntry> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<NotificationLogEntry>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun clear(context: Context) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

}