package com.evapp.android.view.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evapp.android.R
import com.evapp.android.model.User
import com.evapp.android.model.Vendor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class UserProfileFragment : Fragment() {

    private var TAG = "UserProfileFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var user_profile_back_icon: LinearLayout
    lateinit var user_profile_name_input: EditText
    lateinit var user_profile_location_layout: LinearLayout
    lateinit var user_profile_location_input: TextView
    lateinit var user_profile_save_button: Button
    lateinit var user_profile_progress_bar: ProgressBar

    private var address: List<android.location.Address>? = null
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private var postal = ""
    private var area = ""
    private var city = ""
    private var state = ""
    private var country = ""
    private var latitude = 0.0F
    private var longitude = 0.0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        user_profile_back_icon = view.findViewById(R.id.user_profile_back_icon)
        user_profile_name_input = view.findViewById(R.id.user_profile_name_input)
        user_profile_location_layout = view.findViewById(R.id.user_profile_location_layout)
        user_profile_location_input = view.findViewById(R.id.user_profile_location_input)
        user_profile_save_button = view.findViewById(R.id.user_profile_save_button)
        user_profile_progress_bar = view.findViewById(R.id.user_profile_progress_bar)

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        user_profile_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_profile")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_profile", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        fetchDetails()

        user_profile_location_layout.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (hasGps && hasNetwork) {
                    currentLocation()
                } else {
                    Toast.makeText(requireContext(), "Enable GPS & Internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 101)
            }
        }

        user_profile_save_button.setOnClickListener {
            confirmDetails()
        }

        return view

    }//onCreate()

    private fun fetchDetails() {
        database.getReference("user").child("account").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mAccount = dataSnapshot.getValue(User::class.java)!!
                    user_profile_name_input.setText(mAccount.name, TextView.BufferType.EDITABLE)
                    user_profile_location_input.text = "${mAccount.area}, ${mAccount.city}, ${mAccount.state}"
                    user_profile_progress_bar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validateName(): Boolean {
        val nameInput = user_profile_name_input.text.toString().trim()
        return if (nameInput.isEmpty()) {
            Toast.makeText(requireContext(), "Field can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun confirmDetails() {
        if (!validateName()) {
            return
        } else {
            val nameInput = user_profile_name_input.text.toString().trim()

            database.getReference("user").child("account").child(uid)
            .child("name").setValue(nameInput)

            Toast.makeText(requireContext(), "Details Saved", Toast.LENGTH_SHORT).show()
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_profile")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_profile", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }
    }

    @SuppressLint("MissingPermission", "CutPasteId", "SetTextI18n")
    private fun currentLocation() {
        if (hasGps) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                10F, gpsLocationListener)

            val lastKnownLocationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocationByGps?.let {
                locationGps = lastKnownLocationByGps
            }
        } else {
            Toast.makeText(requireContext(), "Please enable GPS", Toast.LENGTH_SHORT).show()
        }

        if (hasNetwork) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000,
                10F, networkLocationListener)

            val lastKnownLocationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastKnownLocationByNetwork?.let {
                locationNetwork = lastKnownLocationByNetwork
            }
        } else {
            Toast.makeText(requireContext(), "Please enable Network", Toast.LENGTH_SHORT).show()
        }

        if (locationGps != null && locationNetwork != null) {
            if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                /*Gps*/
                val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                address = geocoder.getFromLocation(locationGps!!.latitude, locationGps!!.longitude, 1)

                state = address!![0].adminArea
                city = address!![0].locality
                area = address!![0].subLocality
                postal = address!![0].postalCode
                latitude = address!![0].latitude.toFloat()
                longitude = address!![0].longitude.toFloat()
                country = address!![0].countryName

                if (state.isNotEmpty() && city.isNotEmpty() && area.isNotEmpty() &&
                    postal.isNotEmpty() && latitude != 0F && longitude != 0F &&
                    country.isNotEmpty()) {
                    user_profile_location_input.text = "$area, $city, $state"

                    val ref = database.getReference("user").child("account").child(uid)
                    ref.child("area").setValue(area)
                    ref.child("city").setValue(city)
                    ref.child("state").setValue(state)
                    ref.child("country").setValue(country)
                    ref.child("latitude").setValue(latitude)
                    ref.child("longitude").setValue(longitude)
                    ref.child("postal").setValue(postal)
                }
            } else {
                /*Network*/
                val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                address = geocoder.getFromLocation(locationNetwork!!.latitude, locationNetwork!!.longitude, 1)

                state = address!![0].adminArea
                city = address!![0].locality
                area = address!![0].subLocality
                postal = address!![0].postalCode
                latitude = address!![0].latitude.toFloat()
                longitude = address!![0].longitude.toFloat()
                country = address!![0].countryName

                if (state.isNotEmpty() && city.isNotEmpty() && area.isNotEmpty() &&
                    postal.isNotEmpty() && latitude != 0F && longitude != 0F &&
                    country.isNotEmpty()) {
                    user_profile_location_input.text = "$area, $city, $state"

                    val ref = database.getReference("user").child("account").child(uid)
                    ref.child("area").setValue(area)
                    ref.child("city").setValue(city)
                    ref.child("state").setValue(state)
                    ref.child("country").setValue(country)
                    ref.child("latitude").setValue(latitude)
                    ref.child("longitude").setValue(longitude)
                    ref.child("postal").setValue(postal)
                }
            }
        }
    }

    val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationGps = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    val networkLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationNetwork = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            101 -> {
                if (grantResults.count() >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}