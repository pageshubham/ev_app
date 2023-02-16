package com.evapp.android.view.vendor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.evapp.android.R
import com.evapp.android.model.Booking
import com.evapp.android.model.Station
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
class VendorHomeFragment : Fragment() {

    private var TAG = "VendorHomeFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var vendor_home_center_layout: LinearLayout
    lateinit var vendor_home_total_income: TextView
    lateinit var vendor_home_active_port: TextView
    lateinit var vendor_home_total_port: TextView
    lateinit var vendor_home_progress_bar: ProgressBar

    private var mActivePort: MutableList<String> = ArrayList()
    private var mTotalPort = 0
    private var mTotalCost = 0

    private var currentDate = ""
    private var dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vendor_home, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        vendor_home_center_layout = view.findViewById(R.id.vendor_home_center_layout)
        vendor_home_total_income = view.findViewById(R.id.vendor_home_total_income)
        vendor_home_active_port = view.findViewById(R.id.vendor_home_active_port)
        vendor_home_total_port = view.findViewById(R.id.vendor_home_total_port)
        vendor_home_progress_bar = view.findViewById(R.id.vendor_home_progress_bar)

        currentDate = dateFormat.format(Date())
        fetchTotalIncome()
        fetchActivePort()
        fetchTotalPort()

        return view

    }//onCreate()

    private fun fetchTotalIncome() {
        database.getReference("vendor").child("booking").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        mTotalCost += Integer.valueOf(mBooking.total_cost)
                    }
                    vendor_home_total_income.text = "₹ $mTotalCost"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchActivePort() {
        database.getReference("vendor").child("booking").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mActivePort.clear()
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        if (currentDate == dateFormat.format(mBooking.timestamp) && mBooking.status == "Active"){
                            mActivePort.add(mBooking.booking_id)
                        }
                    }
                    vendor_home_active_port.text = "${mActivePort.count()}"
                    vendor_home_center_layout.visibility = View.VISIBLE
                    vendor_home_progress_bar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchTotalPort() {
        database.getReference("vendor").child("station").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val mStation = singleSnapshot.getValue(Station::class.java)!!
                        mTotalPort += Integer.valueOf(mStation.port)
                    }
                    vendor_home_total_port.text = "Active points of $mTotalPort"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}