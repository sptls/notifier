package com.example.notifier

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Button
import android.widget.Toast

//import android.widget.TextView

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
        }

        adapter = NotificationAdapter(NotificationStorage.getAllEntries(context).reversed())

        recyclerView.adapter = adapter

        val clearButton = Button(context).apply {
            text = "Clear Notifications"
            setOnClickListener {
                NotificationStorage.clear(context)
                adapter.update(emptyList())
                Toast.makeText(context, "Notifications cleared", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(clearButton)
        layout.addView(recyclerView)

        return layout
    }

    override fun onResume() {
        super.onResume()
        adapter.update(NotificationStorage.getAllEntries(requireContext()))
        recyclerView.scrollToPosition(0)
    }
}