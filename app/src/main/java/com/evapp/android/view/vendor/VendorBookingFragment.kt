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
import com.evapp.android.model.Booking
import com.evapp.android.model.Station
import com.evapp.android.viewmodel.UserBookingAdapter
import com.evapp.android.viewmodel.VendorStationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@SuppressLint("NotifyDataSetChanged")
class VendorBookingFragment : Fragment() {

    private var TAG = "VendorBookingFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var vendor_booking_recycler_view: RecyclerView
    lateinit var vendor_booking_progress_bar: ProgressBar

    private var mBookingList: MutableList<Booking> = ArrayList()
    private var station_id = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vendor_booking, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        station_id = requireArguments().getString("station_id").toString()

        vendor_booking_recycler_view = view.findViewById(R.id.vendor_booking_recycler_view)
        vendor_booking_progress_bar = view.findViewById(R.id.vendor_booking_progress_bar)

        fetchBooking()
        addChildEventListener()

        return view

    }//onCreate()

    private fun fetchBooking() {
        database.getReference("vendor").child("booking").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mBookingList.clear()
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        if (mBooking.station_id == station_id) {
                            mBookingList.add(mBooking)
                            mBookingList.sortByDescending { it.timestamp.toString() }
                        }
                    }

                    vendor_booking_recycler_view.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    vendor_booking_recycler_view.adapter = UserBookingAdapter(mBookingList, "vendor_booking")
                    (vendor_booking_recycler_view.adapter as UserBookingAdapter).notifyDataSetChanged()

                    vendor_booking_progress_bar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addChildEventListener() {
        database.getReference("vendor").child("booking").child(uid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    fetchBooking()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }

}