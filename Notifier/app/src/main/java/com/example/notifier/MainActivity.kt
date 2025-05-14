package com.example.notifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private val tabTitles = arrayOf("Notifications", "Settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent?.let {
            if (it.getBooleanExtra("open_detail", false)) {
                val detailIntent = Intent(this, NotificationDetailActivity::class.java).apply {
                    putExtra("title", it.getStringExtra("title"))
                    putExtra("body", it.getStringExtra("body"))
                }
                startActivity(detailIntent)
            }
        }


        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> NotificationsFragment()
                    1 -> SettingsFragment()
                    else -> NotificationsFragment()
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}