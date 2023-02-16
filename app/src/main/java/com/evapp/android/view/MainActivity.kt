package com.evapp.android.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.evapp.android.R
import com.evapp.android.model.Place
import com.evapp.android.model.UserMap
import com.evapp.android.viewmodel.MapAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userMap = generateSampleData()
        // set layout manager on recycler view
        recycler_view.layoutManager = LinearLayoutManager(this)
        // set adapter on recycler view
        recycler_view.adapter = MapAdapter(this, userMap, object : MapAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                // when user tap on view in recycler view, navigate to new activity
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra("EXTRA_USER_MAP", userMap[position])
                startActivity(intent)
            }
        })

    }//onCreate()

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap("Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at stanford", 37.426, -122.163),
                    Place("Gates CS Building", "Many long time in basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            )
        )
    }
}