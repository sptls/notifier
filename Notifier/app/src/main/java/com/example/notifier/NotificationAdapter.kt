package com.example.notifier

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class NotificationAdapter(
    private var items: List<NotificationLogEntry>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val body: TextView = view.findViewById(android.R.id.text2)
    }

    fun update(newItems: List<NotificationLogEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        val context = holder.itemView.context
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))

        holder.title.text = "${entry.title} – $date"
        holder.body.text = entry.body

        holder.itemView.setOnClickListener {
            val intent = Intent(context, NotificationDetailActivity::class.java).apply {
                putExtra("title", entry.title)
                putExtra("body", entry.body)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}