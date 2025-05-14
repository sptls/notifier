package com.example.notifier

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(NotificationStorage.getAllEntries(requireContext()))
        recyclerView.adapter = adapter
        return recyclerView
    }

    override fun onResume() {
        super.onResume()
        adapter.update(NotificationStorage.getAllEntries(requireContext()))
    }
}