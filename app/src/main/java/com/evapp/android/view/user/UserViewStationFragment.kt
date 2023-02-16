package com.evapp.android.view.user

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.evapp.android.R
import com.evapp.android.model.Booking
import com.evapp.android.model.Station
import com.evapp.android.model.User
import com.evapp.android.view.auth.SelectionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserViewStationFragment : Fragment() {

    private var TAG = "UserViewStationFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    lateinit var timestamp: Any
    private var uid = ""

    lateinit var user_view_station_back_icon: LinearLayout
    lateinit var user_view_station_title: TextView
    lateinit var user_view_station_center_layout: RelativeLayout
    lateinit var user_view_station_kilometer: TextView
    lateinit var user_view_station_available_port: TextView
    lateinit var user_view_station_charging_cost: TextView
    lateinit var user_view_station_navigation_button: Button
    lateinit var user_view_station_book_slot_button: Button
    lateinit var user_view_station_progress_bar: ProgressBar

    lateinit var book_slot_dialog: Dialog
    private lateinit var single_user_slot_booking_close_icon: LinearLayout
    private lateinit var single_user_slot_booking_company_spinner: AppCompatSpinner
    private lateinit var single_user_slot_booking_model_spinner: AppCompatSpinner
    private lateinit var single_user_slot_booking_type_spinner: AppCompatSpinner
    private lateinit var single_user_slot_booking_time_spinner: AppCompatSpinner
    private lateinit var single_user_slot_booking_availability: TextView
    private lateinit var single_user_slot_booking_confirm_button: Button

    lateinit var booked_slot_dialog: Dialog
    private lateinit var single_user_slot_booked_navigation: Button
    private lateinit var single_user_slot_booked_my_booking: TextView

    private var booking_id = ""
    private var user_id = ""
    private var vendor_id = ""
    private var station_id = ""
    private var user_name = ""
    private var vendor_name = ""
    private var station_name = ""
    private var area = ""
    private var city = ""
    private var state = ""
    private var country = ""
    private var postal = ""
    private var latitude = 0.0
    private var longitude = 0.0
    private var charging_cost = ""
    private var kilometer = ""
    private var available_port = ""
    private var user_latitude = 0.0
    private var user_longitude = 0.0
    private var currentDate = ""

    private var vehicleCompany = ""
    private var vehicleModel = ""
    private var portType = ""
    private var slotTime = ""
    private var mVehicleCompany: MutableList<String> = ArrayList()
    private var mVehicleModel: MutableList<String> = ArrayList()
    private var mPortType: MutableList<String> = ArrayList()
    private var mSlotTime: MutableList<String> = ArrayList()
    private var mActivePort: MutableList<String> = java.util.ArrayList()

    private var dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_view_station, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        timestamp = ServerValue.TIMESTAMP
        uid = auth.currentUser!!.uid

        user_view_station_back_icon = view.findViewById(R.id.user_view_station_back_icon)
        user_view_station_title = view.findViewById(R.id.user_view_station_title)
        user_view_station_center_layout = view.findViewById(R.id.user_view_station_center_layout)
        user_view_station_kilometer = view.findViewById(R.id.user_view_station_kilometer)
        user_view_station_available_port = view.findViewById(R.id.user_view_station_available_port)
        user_view_station_charging_cost = view.findViewById(R.id.user_view_station_charging_cost)
        user_view_station_navigation_button = view.findViewById(R.id.user_view_station_navigation_button)
        user_view_station_book_slot_button = view.findViewById(R.id.user_view_station_book_slot_button)
        user_view_station_progress_bar = view.findViewById(R.id.user_view_station_progress_bar)

        //book_slot_dialog
        book_slot_dialog = Dialog(requireContext())
        book_slot_dialog.setContentView(R.layout.single_user_slot_booking_dialog)
        single_user_slot_booking_close_icon = book_slot_dialog.findViewById(R.id.single_user_slot_booking_close_icon)
        single_user_slot_booking_company_spinner = book_slot_dialog.findViewById(R.id.single_user_slot_booking_company_spinner)
        single_user_slot_booking_model_spinner = book_slot_dialog.findViewById(R.id.single_user_slot_booking_model_spinner)
        single_user_slot_booking_type_spinner = book_slot_dialog.findViewById(R.id.single_user_slot_booking_type_spinner)
        single_user_slot_booking_time_spinner = book_slot_dialog.findViewById(R.id.single_user_slot_booking_time_spinner)
        single_user_slot_booking_availability = book_slot_dialog.findViewById(R.id.single_user_slot_booking_availability)
        single_user_slot_booking_confirm_button = book_slot_dialog.findViewById(R.id.single_user_slot_booking_confirm_button)
        book_slot_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        book_slot_dialog.setCancelable(true)

        //booked_slot_dialog
        booked_slot_dialog = Dialog(requireContext())
        booked_slot_dialog.setContentView(R.layout.single_user_slot_booked_dialog)
        single_user_slot_booked_navigation = booked_slot_dialog.findViewById(R.id.single_user_slot_booked_navigation)
        single_user_slot_booked_my_booking = booked_slot_dialog.findViewById(R.id.single_user_slot_booked_my_booking)
        booked_slot_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        booked_slot_dialog.setCancelable(true)

        booking_id = requireArguments().getString("booking_id").toString()
        vendor_id = requireArguments().getString("vendor_id").toString()
        station_id = requireArguments().getString("station_id").toString()
        vendor_name = requireArguments().getString("vendor_name").toString()
        station_name = requireArguments().getString("station_name").toString()
        area = requireArguments().getString("area").toString()
        city = requireArguments().getString("city").toString()
        state = requireArguments().getString("state").toString()
        country = requireArguments().getString("country").toString()
        postal = requireArguments().getString("postal").toString()
        latitude = requireArguments().getDouble("latitude")
        longitude = requireArguments().getDouble("longitude")
        charging_cost = requireArguments().getString("charging_cost").toString()
        kilometer = requireArguments().getString("kilometer").toString()
        available_port = requireArguments().getString("available_port").toString()

        user_view_station_title.text = station_name
        user_view_station_kilometer.text = "$kilometer km"
        user_view_station_available_port.text = available_port
        user_view_station_charging_cost.text = "₹$charging_cost / kWh"
        currentDate = dateFormat.format(Date())

        user_view_station_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_view_station")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_view_station", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        user_view_station_navigation_button.setOnClickListener {
            val intent = Intent(requireContext(), UserNavigationActivity::class.java)
            intent.putExtra("user_latitude", user_latitude)
            intent.putExtra("user_longitude", user_longitude)
            intent.putExtra("vendor_latitude", latitude)
            intent.putExtra("vendor_longitude", longitude)
            requireContext().startActivity(intent)

            /*val intent = Intent(requireContext(), UserDirectionActivity::class.java)
            intent.putExtra("user_latitude", user_latitude)
            intent.putExtra("user_longitude", user_longitude)
            intent.putExtra("vendor_latitude", latitude)
            intent.putExtra("vendor_longitude", longitude)
            requireContext().startActivity(intent)*/
        }

        user_view_station_book_slot_button.setOnClickListener {
            showBookSlotDialog()
        }

        fetchUserDetails()

        return view

    }//onCreate()

    private fun fetchUserDetails() {
        database.getReference("user").child("account").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mUser = dataSnapshot.getValue(User::class.java)!!
                    user_id = uid
                    user_name = mUser.name
                    user_latitude = mUser.latitude
                    user_longitude = mUser.longitude

                    user_view_station_center_layout.visibility = View.VISIBLE
                    user_view_station_progress_bar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showBookSlotDialog() {
        book_slot_dialog.show()
        val window = book_slot_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        //company spinner
        mVehicleCompany = resources.getStringArray(R.array.vehicle_company).toMutableList()
        val adapterCompany: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mVehicleCompany)
        adapterCompany.setDropDownViewResource(R.layout.single_spinner_item_layout)
        single_user_slot_booking_company_spinner.adapter = adapterCompany

        single_user_slot_booking_company_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        vehicleCompany = single_user_slot_booking_company_spinner.selectedItem.toString()
                    }
                }
            }

        //model spinner
        mVehicleModel = resources.getStringArray(R.array.vehicle_model).toMutableList()
        val adapterModel: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mVehicleModel)
        adapterModel.setDropDownViewResource(R.layout.single_spinner_item_layout)
        single_user_slot_booking_model_spinner.adapter = adapterModel

        single_user_slot_booking_model_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        vehicleModel = single_user_slot_booking_model_spinner.selectedItem.toString()
                    }
                }
            }

        //port type spinner
        mPortType = resources.getStringArray(R.array.port_type).toMutableList()
        val adapterPort: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mPortType)
        adapterPort.setDropDownViewResource(R.layout.single_spinner_item_layout)
        single_user_slot_booking_type_spinner.adapter = adapterPort

        single_user_slot_booking_type_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        portType = single_user_slot_booking_type_spinner.selectedItem.toString()
                    }
                }
            }

        //slot time spinner
        mSlotTime = resources.getStringArray(R.array.slot_time).toMutableList()
        val adapterFrom: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mSlotTime)
        adapterFrom.setDropDownViewResource(R.layout.single_spinner_item_layout)
        single_user_slot_booking_time_spinner.adapter = adapterFrom

        single_user_slot_booking_time_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        slotTime = single_user_slot_booking_time_spinner.selectedItem.toString()
                    }
                    checkSlotAvailability()
                }
            }

        single_user_slot_booking_close_icon.setOnClickListener {
            book_slot_dialog.dismiss()
        }

        single_user_slot_booking_confirm_button.setOnClickListener {
            confirmBooking()
        }
    }

    private fun confirmBooking() {
        val ref = database.getReference("user").child("booking").child(uid).child(booking_id)
        ref.child("booking_id").setValue(booking_id)
        ref.child("user_id").setValue(user_id)
        ref.child("vendor_id").setValue(vendor_id)
        ref.child("station_id").setValue(station_id)
        ref.child("user_name").setValue(user_name)
        ref.child("vendor_name").setValue(vendor_name)
        ref.child("station_name").setValue(station_name)
        ref.child("area").setValue(area)
        ref.child("city").setValue(city)
        ref.child("state").setValue(state)
        ref.child("country").setValue(country)
        ref.child("postal").setValue(postal)
        ref.child("latitude").setValue(latitude)
        ref.child("longitude").setValue(longitude)
        ref.child("charging_cost").setValue(charging_cost)
        ref.child("total_cost").setValue(charging_cost)
        ref.child("vehicle_company").setValue(vehicleCompany)
        ref.child("vehicle_model").setValue(vehicleModel)
        ref.child("slot_time").setValue(slotTime)
        ref.child("kilometer").setValue(kilometer)
        ref.child("status").setValue("Pending")
        ref.child("timestamp").setValue(timestamp)
            .addOnSuccessListener {
                val fromPath = database.getReference("user").child("booking")
                    .child(uid).child(booking_id)
                val toPath = database.getReference("vendor").child("booking")
                    .child(vendor_id).child(booking_id)

                fromPath.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        toPath.setValue(dataSnapshot.value) { error, ref ->
                            if (error != null) {
                                println("Copy failed")
                            } else {
                                println("Success")
                                book_slot_dialog.dismiss()
                                showBookedSlotDialog()
                            }
                        }
                    }
                })
            }
    }

    private fun showBookedSlotDialog() {
        booked_slot_dialog.show()
        val window = booked_slot_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        single_user_slot_booked_navigation.setOnClickListener {}

        single_user_slot_booked_my_booking.setOnClickListener {
            booked_slot_dialog.dismiss()

            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_view_station")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_view_station", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()

            val transaction1 = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction1.add(R.id.fragment, UserBookingFragment(), "user_booking")
            transaction1.addToBackStack("user_booking")
            transaction1.commit()
        }

    }

    private fun checkSlotAvailability() {
        database.getReference("vendor").child("booking")
            .child(vendor_id).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (singleSnapshot in dataSnapshot.children) {
                        val mBooking = singleSnapshot.getValue(Booking::class.java)!!
                        val bookingDate = dateFormat.format(mBooking.timestamp)

                        if (currentDate == bookingDate && slotTime == mBooking.slot_time) {
                            single_user_slot_booking_availability.text = "Slot isn't Available"
                            single_user_slot_booking_confirm_button.isEnabled = false
                        } else {
                            single_user_slot_booking_availability.text = "Slot is Available"
                            single_user_slot_booking_confirm_button.isEnabled = true
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

}