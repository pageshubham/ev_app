package com.evapp.android.view.user

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evapp.android.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.w3c.dom.Text
import java.util.*

class UserContributeFragment : Fragment() {

    private var TAG = "UserContributeFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var uid = ""

    lateinit var user_contribute_back_icon: LinearLayout
    lateinit var user_contribute_location_layout: LinearLayout
    lateinit var user_contribute_location_input: TextView
    lateinit var user_contribute_image_layout: LinearLayout
    lateinit var user_contribute_image_input: TextView
    lateinit var user_contribute_image_progress_bar: ProgressBar
    lateinit var user_contribute_image_icon: ImageView
    lateinit var user_contribute_add_station_button: Button

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
    lateinit var photoUri: Uri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_contribute, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        uid = auth.currentUser!!.uid

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        user_contribute_back_icon = view.findViewById(R.id.user_contribute_back_icon)
        user_contribute_location_layout = view.findViewById(R.id.user_contribute_location_layout)
        user_contribute_location_input = view.findViewById(R.id.user_contribute_location_input)
        user_contribute_image_layout = view.findViewById(R.id.user_contribute_image_layout)
        user_contribute_image_input = view.findViewById(R.id.user_contribute_image_input)
        user_contribute_image_progress_bar = view.findViewById(R.id.user_contribute_image_progress_bar)
        user_contribute_image_icon = view.findViewById(R.id.user_contribute_image_icon)
        user_contribute_add_station_button = view.findViewById(R.id.user_contribute_add_station_button)

        user_contribute_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_contribute")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_contribute", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        user_contribute_location_layout.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (hasGps && hasNetwork) {
                    currentLocation()
                } else {
                    Toast.makeText(requireContext(), "Enable GPS & Internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 101)
            }
        }

        user_contribute_image_layout.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val i = Intent()
                i.action = Intent.ACTION_GET_CONTENT
                i.type = "image/*"
                startActivityForResult(i, 105)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 102)
            }
        }

        user_contribute_add_station_button.setOnClickListener {
            confirmDetails()
        }

        return view

    }//onCreate()

    private fun validateLocation(): Boolean {
        val locationInput = user_contribute_location_input.text.toString().trim()
        return if (locationInput.isEmpty()) {
            Toast.makeText(requireContext(), "Please selection location", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateImage(): Boolean {
        val imageInput = user_contribute_image_input.text.toString().trim()
        return if (imageInput.isEmpty()) {
            Toast.makeText(requireContext(), "Please upload image", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun confirmDetails() {
        if (!validateLocation() or !validateImage()) {
            return
        } else {
            user_contribute_image_progress_bar.visibility = View.VISIBLE
            user_contribute_image_icon.visibility = View.GONE

            val station_id = database.getReference("user").child("station")
                .child(uid).push().key!!

            val photo = storage.getReference("user").child("station")
                .child(uid).child(station_id).child("image")
            val uploadPhotoTask = photo.putFile(Uri.parse(photoUri.toString()))

            uploadPhotoTask.addOnSuccessListener {
                uploadPhotoTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed to Upload Image", Toast.LENGTH_SHORT).show()
                        user_contribute_image_progress_bar.visibility = View.GONE
                        user_contribute_image_icon.visibility = View.VISIBLE
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation photo.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val photoDownloadUri = task.result!!
                        val ref = database.getReference("user").child("station")
                            .child(uid).child(station_id)
                        ref.child("uid").setValue(uid)
                        ref.child("name").setValue("")
                        ref.child("station_id").setValue(station_id)
                        ref.child("station_name").setValue("")
                        ref.child("postal").setValue(postal)
                        ref.child("area").setValue(area)
                        ref.child("city").setValue(city)
                        ref.child("state").setValue(state)
                        ref.child("country").setValue(country)
                        ref.child("image").setValue(photoDownloadUri.toString())
                        ref.child("cost").setValue("")
                        ref.child("port").setValue("")
                        ref.child("quantity").setValue("")
                        ref.child("latitude").setValue(latitude)
                        ref.child("longitude").setValue(longitude)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Station Added Successfully", Toast.LENGTH_SHORT).show()
                                val fragmentManager = requireActivity().supportFragmentManager
                                val frag = fragmentManager.findFragmentByTag("user_contribute")!!
                                val transaction = fragmentManager.beginTransaction()
                                fragmentManager.popBackStack("user_contribute", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                transaction.remove(frag).commit()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add Station", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(requireContext(), "Failed to Upload Image", Toast.LENGTH_SHORT).show()
                        uploadPhotoTask.cancel()
                    }
                }

            }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    user_contribute_image_input.text = "${progress.toInt()}% uploaded..."
                }.addOnPausedListener {
                    uploadPhotoTask.pause()
                    println("Upload is paused")
                }.addOnCanceledListener {
                    uploadPhotoTask.cancel()
                    user_contribute_image_progress_bar.visibility = View.GONE
                    user_contribute_image_icon.visibility = View.VISIBLE
                }
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
                    user_contribute_location_input.text = "$area, $city, $state"
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
                    user_contribute_location_input.text = "$area, $city, $state"
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
            102 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 105 && resultCode == Activity.RESULT_OK) {
            photoUri = data!!.data!!
            user_contribute_image_input.text = "Image picked"
        }
    }

}