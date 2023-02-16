package com.evapp.android.view.vendor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evapp.android.R
import com.evapp.android.model.Station
import com.evapp.android.model.Vendor
import com.evapp.android.viewmodel.VendorStationAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@SuppressLint("NotifyDataSetChanged")
class VendorStationFragment : Fragment() {

    private var TAG = "VendorStationFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var vendor_station_recycler_view: RecyclerView
    lateinit var vendor_station_add_fab: FloatingActionButton
    lateinit var vendor_station_progress_bar: ProgressBar

    private var mStationList: MutableList<Station> = ArrayList()
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vendor_station, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        vendor_station_recycler_view = view.findViewById(R.id.vendor_station_recycler_view)
        vendor_station_add_fab = view.findViewById(R.id.vendor_station_add_fab)
        vendor_station_progress_bar = view.findViewById(R.id.vendor_station_progress_bar)

        fetchVendorDetails()
        fetchStations()
        addChildEventListener()

        vendor_station_add_fab.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, VendorAddStationFragment(), "vendor_add_station")
            transaction.addToBackStack("vendor_add_station")
            transaction.commit()
        }

        return view

    }//onCreate()

    private fun fetchVendorDetails() {
        database.getReference("vendor").child("account").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mVendor = dataSnapshot.getValue(Vendor::class.java)!!
                    latitude = mVendor.latitude
                    longitude = mVendor.longitude
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchStations() {
        database.getReference("vendor").child("station").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mStationList.clear()
                    for (singleSnapshot in dataSnapshot.children) {
                        val mStation = singleSnapshot.getValue(Station::class.java)!!
                        mStationList.add(mStation)
                    }

                    vendor_station_recycler_view.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    vendor_station_recycler_view.adapter = VendorStationAdapter("station",
                        mStationList, latitude, longitude)
                    (vendor_station_recycler_view.adapter as VendorStationAdapter).notifyDataSetChanged()

                    vendor_station_progress_bar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addChildEventListener() {
        database.getReference("vendor").child("station").child(uid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    fetchStations()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}