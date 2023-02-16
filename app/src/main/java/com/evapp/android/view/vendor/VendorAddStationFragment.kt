package com.evapp.android.view.vendor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
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
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
class VendorAddStationFragment : Fragment() {

    private var TAG = "VendorAddStationFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var uid = ""

    lateinit var vendor_add_station_back_icon: LinearLayout
    lateinit var vendor_add_station_name_input: EditText
    lateinit var vendor_add_station_location_layout: LinearLayout
    lateinit var vendor_add_station_location_input: TextView
    lateinit var vendor_add_station_location_progress_bar: ProgressBar
    lateinit var vendor_add_station_location_icon: ImageView
    lateinit var vendor_add_station_image_layout: LinearLayout
    lateinit var vendor_add_station_image_input: TextView
    lateinit var vendor_add_station_image_progress_bar: ProgressBar
    lateinit var vendor_add_station_image_icon: ImageView
    lateinit var vendor_add_station_cost_input: EditText
    lateinit var vendor_add_station_port_spinner: AppCompatSpinner
    lateinit var vendor_add_station_quantity_spinner: AppCompatSpinner
    lateinit var vendor_add_station_button: Button

    lateinit var add_station_dialog: Dialog
    private lateinit var single_vendor_station_added_ports: TextView
    private lateinit var single_vendor_station_added_button: Button

    lateinit var photoUri: Uri
    private var port = ""
    private var quantity = ""
    private var mPorts: MutableList<String> = ArrayList()
    private var mQuantities: MutableList<String> = ArrayList()

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
        val view = inflater.inflate(R.layout.fragment_vendor_add_station, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        uid = auth.currentUser!!.uid

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        vendor_add_station_back_icon = view.findViewById(R.id.vendor_add_station_back_icon)
        vendor_add_station_name_input = view.findViewById(R.id.vendor_add_station_name_input)
        vendor_add_station_location_layout = view.findViewById(R.id.vendor_add_station_location_layout)
        vendor_add_station_location_input = view.findViewById(R.id.vendor_add_station_location_input)
        vendor_add_station_location_progress_bar = view.findViewById(R.id.vendor_add_station_location_progress_bar)
        vendor_add_station_location_icon = view.findViewById(R.id.vendor_add_station_location_icon)
        vendor_add_station_image_layout = view.findViewById(R.id.vendor_add_station_image_layout)
        vendor_add_station_image_input = view.findViewById(R.id.vendor_add_station_image_input)
        vendor_add_station_image_progress_bar = view.findViewById(R.id.vendor_add_station_image_progress_bar)
        vendor_add_station_image_icon = view.findViewById(R.id.vendor_add_station_image_icon)
        vendor_add_station_cost_input = view.findViewById(R.id.vendor_add_station_cost_input)
        vendor_add_station_port_spinner = view.findViewById(R.id.vendor_add_station_port_spinner)
        vendor_add_station_quantity_spinner = view.findViewById(R.id.vendor_add_station_quantity_spinner)
        vendor_add_station_button = view.findViewById(R.id.vendor_add_station_button)

        //add_station_dialog
        add_station_dialog = Dialog(requireContext())
        add_station_dialog.setContentView(R.layout.single_vendor_station_added_dialog)
        single_vendor_station_added_ports = add_station_dialog.findViewById(R.id.single_vendor_station_added_ports)
        single_vendor_station_added_button = add_station_dialog.findViewById(R.id.single_vendor_station_added_button)
        add_station_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        add_station_dialog.setCancelable(true)

        vendor_add_station_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("vendor_add_station")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("vendor_add_station", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        vendor_add_station_location_layout.setOnClickListener {
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

        vendor_add_station_image_layout.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val i = Intent()
                i.action = Intent.ACTION_GET_CONTENT
                i.type = "image/*"
                startActivityForResult(i, 105)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 102)
            }
        }

        mPorts = resources.getStringArray(R.array.ports).toMutableList()
        val adapterPort: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mPorts)
        adapterPort.setDropDownViewResource(R.layout.single_spinner_item_layout)
        vendor_add_station_port_spinner.adapter = adapterPort

        vendor_add_station_port_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        port = vendor_add_station_port_spinner.selectedItem.toString()
                    }
                }
            }

        mQuantities = resources.getStringArray(R.array.quantity).toMutableList()
        val adapterQuantity: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mQuantities)
        adapterQuantity.setDropDownViewResource(R.layout.single_spinner_item_layout)
        vendor_add_station_quantity_spinner.adapter = adapterQuantity

        vendor_add_station_quantity_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        quantity = vendor_add_station_quantity_spinner.selectedItem.toString()
                    }
                }
            }

        vendor_add_station_button.setOnClickListener {
            confirmDetails()
        }

        return view

    }//onCreate()

    private fun validateName(): Boolean {
        val nameInput = vendor_add_station_name_input.text.toString().trim()
        return if (nameInput.isEmpty()) {
            Toast.makeText(requireContext(), "Field can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateLocation(): Boolean {
        val locationInput = vendor_add_station_location_input.text.toString().trim()
        return if (locationInput.isEmpty()) {
            Toast.makeText(requireContext(), "Please selection location", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateImage(): Boolean {
        val imageInput = vendor_add_station_image_input.text.toString().trim()
        return if (imageInput.isEmpty()) {
            Toast.makeText(requireContext(), "Please upload image", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateCost(): Boolean {
        val costInput = vendor_add_station_cost_input.text.toString().trim()
        return if (costInput.isEmpty()) {
            Toast.makeText(requireContext(), "Field can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validatePort(): Boolean {
        return if (port == "Port") {
            Toast.makeText(requireContext(), "Please select port", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateQuantity(): Boolean {
        return if (quantity == "Quantity") {
            Toast.makeText(requireContext(), "Please select quantity", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun confirmDetails() {
        if (!validateName() or !validateLocation() or !validateImage() or
            !validateCost() or !validatePort() or !validateQuantity()) {
            return
        } else {
            val nameInput = vendor_add_station_name_input.text.toString().trim()
            val costInput = vendor_add_station_cost_input.text.toString().trim()

            vendor_add_station_image_progress_bar.visibility = View.VISIBLE
            vendor_add_station_image_icon.visibility = View.GONE

            val station_id = database.getReference("vendor").child("station")
                .child(uid).push().key!!

            val photo = storage.getReference("vendor").child("station")
                .child(uid).child(station_id).child("image")
            val uploadPhotoTask = photo.putFile(Uri.parse(photoUri.toString()))

            uploadPhotoTask.addOnSuccessListener {
                uploadPhotoTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(requireContext(), "Failed to Upload Image", Toast.LENGTH_SHORT).show()
                        vendor_add_station_image_progress_bar.visibility = View.GONE
                        vendor_add_station_image_icon.visibility = View.VISIBLE
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation photo.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val photoDownloadUri = task.result!!
                        val ref = database.getReference("vendor").child("station")
                            .child(uid).child(station_id)
                        ref.child("uid").setValue(uid)
                        ref.child("name").setValue("")
                        ref.child("station_id").setValue(station_id)
                        ref.child("station_name").setValue(nameInput)
                        ref.child("postal").setValue(postal)
                        ref.child("area").setValue(area)
                        ref.child("city").setValue(city)
                        ref.child("state").setValue(state)
                        ref.child("country").setValue(country)
                        ref.child("image").setValue(photoDownloadUri.toString())
                        ref.child("cost").setValue(costInput)
                        ref.child("port").setValue(port)
                        ref.child("quantity").setValue(quantity)
                        ref.child("latitude").setValue(latitude)
                        ref.child("longitude").setValue(longitude)
                            .addOnSuccessListener {
                                showAddStationDialog()
                                val fragmentManager = requireActivity().supportFragmentManager
                                val frag = fragmentManager.findFragmentByTag("vendor_add_station")!!
                                val transaction = fragmentManager.beginTransaction()
                                fragmentManager.popBackStack("vendor_add_station", FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
                    vendor_add_station_image_input.text = "${progress.toInt()}% uploaded..."
                }.addOnPausedListener {
                    uploadPhotoTask.pause()
                    println("Upload is paused")
                }.addOnCanceledListener {
                    uploadPhotoTask.cancel()
                    vendor_add_station_image_progress_bar.visibility = View.GONE
                    vendor_add_station_image_icon.visibility = View.VISIBLE
                }
        }
    }

    private fun showAddStationDialog() {
        add_station_dialog.show()
        val window = add_station_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        val nameInput = vendor_add_station_name_input.text.toString().trim()
        single_vendor_station_added_ports.text = "$nameInput with $port"

        single_vendor_station_added_button.setOnClickListener {
            add_station_dialog.dismiss()
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
                    vendor_add_station_location_input.text = "$area, $city, $state"
                    vendor_add_station_location_progress_bar.visibility = View.GONE
                    vendor_add_station_location_icon.visibility = View.VISIBLE
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
                    vendor_add_station_location_input.text = "$area, $city, $state"
                    vendor_add_station_location_progress_bar.visibility = View.GONE
                    vendor_add_station_location_icon.visibility = View.VISIBLE
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
            vendor_add_station_image_input.text = "Image picked"
        }
    }

}