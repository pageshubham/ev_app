package com.evapp.android.viewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.evapp.android.R
import com.evapp.android.model.Booking
import com.evapp.android.model.Station
import com.evapp.android.view.user.UserViewStationFragment
import com.evapp.android.view.vendor.VendorBookingFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.single_user_booking_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("SetTextI18n")
class VendorStationAdapter(private val fragment: String, private val mStationList: MutableList<Station>,
private val latitude: Double, private val longitude: Double)
    : RecyclerView.Adapter<VendorStationAdapter.ViewHolder>() {

    private var TAG = "VendorStationAdapter"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""
    lateinit var context: Context

    lateinit var ticket_dialog: Dialog
    private lateinit var single_user_slot_ticket_close_icon: LinearLayout

    private var mActivePort: MutableList<String> = ArrayList()
    private var mTotalPort = 0
    private var currentDate = ""
    private var mDistance = 0
    private var dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_user_booking_layout, parent, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid
        context = parent.context

        currentDate = dateFormat.format(Date())

        //ticket_dialog
        ticket_dialog = Dialog(context)
        ticket_dialog.setContentView(R.layout.single_user_slot_ticket_dialog)
        single_user_slot_ticket_close_icon = ticket_dialog.findViewById(R.id.single_user_slot_ticket_close_icon)
        ticket_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ticket_dialog.setCancelable(true)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.single_user_delete_icon.visibility = View.GONE
        when(fragment) {
            "home" -> {
                holder.view.single_user_booking_title.text = mStationList[position].station_name
                fetchActivePort(mStationList[position].uid, mStationList[position].station_id,
                    mStationList[position].port, holder.view.single_user_booking_slot)
            }
            "station" -> {
                holder.view.single_user_booking_title.text = mStationList[position].station_name
                holder.view.single_user_booking_slot.text = mStationList[position].city
            }
        }

        getKilometer(mStationList[position].latitude, mStationList[position].longitude,
            latitude, longitude, holder.view.single_user_booking_kilometer)

        holder.view.setOnClickListener {
            when(fragment) {
                "home" -> {
                    val userViewStationFragment = UserViewStationFragment()
                    val bundle = Bundle()
                    bundle.putString("booking_id", database.getReference("vendor")
                        .child("station").child(mStationList[position].uid)
                        .child(mStationList[position].station_id).push().key!!)
                    bundle.putString("vendor_id", mStationList[position].uid)
                    bundle.putString("station_id", mStationList[position].station_id)
                    bundle.putString("vendor_name", mStationList[position].name)
                    bundle.putString("station_name", mStationList[position].station_name)
                    bundle.putString("area", mStationList[position].area)
                    bundle.putString("city", mStationList[position].city)
                    bundle.putString("state", mStationList[position].state)
                    bundle.putString("country", mStationList[position].country)
                    bundle.putString("postal", mStationList[position].postal)
                    bundle.putDouble("latitude", mStationList[position].latitude)
                    bundle.putDouble("longitude", mStationList[position].longitude)
                    bundle.putString("charging_cost", mStationList[position].cost)
                    bundle.putString("kilometer", "$mDistance")
                    bundle.putString("available_port", "${mStationList[position].port.toInt() - mActivePort.count()}")
                    userViewStationFragment.arguments = bundle
                    val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    transaction.add(R.id.fragment, userViewStationFragment, "user_view_station")
                    transaction.addToBackStack("user_view_station")
                    transaction.commit()
                }
                "station" -> {
                    val vendorBookingFragment = VendorBookingFragment()
                    val bundle = Bundle()
                    bundle.putString("station_id", mStationList[position].station_id)
                    vendorBookingFragment.arguments = bundle
                    val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    transaction.add(R.id.fragment, vendorBookingFragment, "vendor_view_station")
                    transaction.addToBackStack("vendor_view_station")
                    transaction.commit()
                }
            }
        }
    }

    override fun getItemCount(): Int = mStationList.size

    private fun fetchActivePort(uid: String, station_id: String, mTotalPort: String, booking_slot: TextView) {
        database.getReference("vendor").child("booking").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mActivePort.clear()
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        if (currentDate == dateFormat.format(Date(mBooking.timestamp as Long)) &&
                            mBooking.status == "Pending" && mBooking.station_id == station_id){
                            mActivePort.add(mBooking.booking_id)
                        }
                    }
                    booking_slot.text = "${mTotalPort.toInt() - mActivePort.count()} Available"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getKilometer(lat1: Double, lon1: Double, lat2: Double, lon2: Double, kilometer: TextView): Double {
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1)) * sin(deg2rad(lat2)) + (cos(deg2rad(lat1))
                * cos(deg2rad(lat2)) * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        kilometer.text = "${dist.toInt()} km"
        mDistance = dist.toInt()
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

}