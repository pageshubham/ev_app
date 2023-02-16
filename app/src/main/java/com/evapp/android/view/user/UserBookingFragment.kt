package com.evapp.android.view.user

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
class UserBookingFragment : Fragment() {

    private var TAG = "UserBookingFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var user_booking_recycler_view: RecyclerView
    lateinit var user_booking_progress_bar: ProgressBar

    private var mBookingList: MutableList<Booking> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_booking, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        user_booking_recycler_view = view.findViewById(R.id.user_booking_recycler_view)
        user_booking_progress_bar = view.findViewById(R.id.user_booking_progress_bar)

        fetchBooking()
        addChildEventListener()

        return view

    }//onCreate()

    private fun fetchBooking() {
        database.getReference("user").child("booking").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mBookingList.clear()
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        if (mBooking.status == "Pending") {
                            mBookingList.add(mBooking)
                            mBookingList.sortByDescending { it.timestamp.toString() }
                        }
                    }

                    user_booking_recycler_view.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    user_booking_recycler_view.adapter = UserBookingAdapter(mBookingList, "user_booking")
                    (user_booking_recycler_view.adapter as UserBookingAdapter).notifyDataSetChanged()

                    user_booking_progress_bar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addChildEventListener() {
        database.getReference("user").child("booking").child(uid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    fetchBooking()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    fetchBooking()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    fetchBooking()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}

            })
    }

}