package com.evapp.android.viewmodel

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.evapp.android.R
import com.evapp.android.model.Booking
import com.evapp.android.model.Station
import com.evapp.android.view.user.UserViewStationFragment
import com.evapp.android.view.vendor.VendorProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.single_user_booking_layout.view.*

@SuppressLint("SetTextI18n")
class UserBookingAdapter(private val mBookingList: MutableList<Booking>, private val adapter: String)
    : RecyclerView.Adapter<UserBookingAdapter.ViewHolder>() {

    private var TAG = "UserBookingAdapter"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""
    lateinit var context: Context

    lateinit var ticket_dialog: Dialog
    private lateinit var single_user_slot_ticket_close_icon: LinearLayout
    private lateinit var single_user_slot_ticket_name: TextView
    private lateinit var single_user_slot_booked_timing: TextView
    private lateinit var single_user_slot_ticket_scan_button: Button
    private lateinit var single_user_slot_ticket_navigation_button: Button

    lateinit var plug_in_dialog: Dialog
    private lateinit var single_user_plug_in_timing: TextView
    private lateinit var single_user_plug_in_done: Button

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_user_booking_layout, parent, false)

        context = parent.context
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        //ticket_dialog
        ticket_dialog = Dialog(context)
        ticket_dialog.setContentView(R.layout.single_user_slot_ticket_dialog)
        single_user_slot_ticket_close_icon = ticket_dialog.findViewById(R.id.single_user_slot_ticket_close_icon)
        single_user_slot_ticket_name = ticket_dialog.findViewById(R.id.single_user_slot_ticket_name)
        single_user_slot_booked_timing = ticket_dialog.findViewById(R.id.single_user_slot_booked_timing)
        single_user_slot_ticket_scan_button = ticket_dialog.findViewById(R.id.single_user_slot_ticket_scan_button)
        single_user_slot_ticket_navigation_button = ticket_dialog.findViewById(R.id.single_user_slot_ticket_navigation_button)
        ticket_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ticket_dialog.setCancelable(true)

        //plug_in_dialog
        plug_in_dialog = Dialog(context)
        plug_in_dialog.setContentView(R.layout.single_user_plug_in_dialog)
        single_user_plug_in_timing = plug_in_dialog.findViewById(R.id.single_user_plug_in_timing)
        single_user_plug_in_done = plug_in_dialog.findViewById(R.id.single_user_plug_in_done)
        plug_in_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        plug_in_dialog.setCancelable(false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(adapter) {
            "user_booking" -> {
                holder.view.single_user_booking_title.text = mBookingList[position].station_name
                holder.view.single_user_booking_slot.text = "from ${mBookingList[position].slot_time}"
                holder.view.single_user_booking_kilometer.text = "${mBookingList[position].kilometer} km"
            }
            "vendor_booking" -> {
                holder.view.single_user_booking_title.text = "${mBookingList[position].vehicle_company}\n${mBookingList[position].vehicle_model}"
                holder.view.single_user_booking_slot.text = mBookingList[position].user_name
                holder.view.single_user_booking_kilometer.text = "From: ${mBookingList[position].slot_time}"
            }
        }

        holder.view.setOnClickListener {
            when(adapter) {
                "user_booking" -> {
                    if (mBookingList[position].status == "Pending") {
                        showSlotTicketDialog(mBookingList[position].station_name,
                            mBookingList[position].slot_time, mBookingList[position].booking_id, mBookingList[position].vendor_id)
                    } else {
                        Toast.makeText(context, "Charging Done", Toast.LENGTH_SHORT).show()
                    }
                }
                "vendor_booking" -> {

                }
            }
        }

        holder.view.single_user_delete_icon.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage("Do you want to delete this booking ?")
                .setCancelable(false)
                .setPositiveButton("Proceed") { dialog, id ->
                    when(adapter) {
                        "user_booking" -> {
                            database.getReference("user").child("booking").child(mBookingList[position].user_id)
                                .child(mBookingList[position].booking_id).child("status").setValue("Cancelled")
                            database.getReference("vendor").child("booking").child(mBookingList[position].vendor_id)
                                .child(mBookingList[position].booking_id).child("status").setValue("Cancelled")

                            val cutCharge = ((mBookingList[position].total_cost.toDouble() / 100) * 50).toInt()

                            database.getReference("user").child("booking").child(mBookingList[position].user_id)
                                .child(mBookingList[position].booking_id).child("total_cost").setValue("$cutCharge")
                            database.getReference("vendor").child("booking").child(mBookingList[position].vendor_id)
                                .child(mBookingList[position].booking_id).child("total_cost").setValue("$cutCharge")

                            Toast.makeText(context, "Booking Deleted", Toast.LENGTH_SHORT).show()
                        }
                        "vendor_booking" -> {
                            database.getReference("user").child("booking").child(mBookingList[position].user_id)
                                .child(mBookingList[position].booking_id).child("status").setValue("Cancelled")
                            database.getReference("vendor").child("booking").child(mBookingList[position].vendor_id)
                                .child(mBookingList[position].booking_id).child("status").setValue("Cancelled")
                            Toast.makeText(context, "Booking Deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    dialog.cancel()
                }

            val alert = dialogBuilder.create()
            alert.setTitle("Delete Booking")
            alert.show()
        }
    }

    override fun getItemCount(): Int = mBookingList.size

    private fun showSlotTicketDialog(station_name: String, slot_time: String, booking_id: String, vendor_id: String) {
        ticket_dialog.show()
        val window = ticket_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        single_user_slot_ticket_close_icon.setOnClickListener {
            ticket_dialog.dismiss()
        }

        single_user_slot_ticket_name.text = station_name
        single_user_slot_booked_timing.text = "from $slot_time"

        single_user_slot_ticket_scan_button.setOnClickListener {
            database.getReference("user").child("booking").child(uid).child(booking_id)
                .child("status").setValue("Active")
            database.getReference("vendor").child("booking").child(vendor_id).child(booking_id)
                .child("status").setValue("Active")

            ticket_dialog.dismiss()
            showPlugInDialog(station_name, slot_time, booking_id, vendor_id)
        }
    }

    private fun showPlugInDialog(station_name: String, slot_time: String, booking_id: String, vendor_id: String) {
        plug_in_dialog.show()
        val window = plug_in_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        single_user_plug_in_timing.text = "$station_name \nfrom $slot_time"

        single_user_plug_in_done.setOnClickListener {
            database.getReference("user").child("booking").child(uid).child(booking_id)
                .child("status").setValue("Finished")
            database.getReference("vendor").child("booking").child(vendor_id).child(booking_id)
                .child("status").setValue("Finished")
            plug_in_dialog.dismiss()
        }
    }

    private fun getKilometer() {
        //find kilometer distance between two points
    }

}