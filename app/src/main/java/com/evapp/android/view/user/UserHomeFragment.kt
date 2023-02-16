package com.evapp.android.view.user

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evapp.android.R
import com.evapp.android.model.Place
import com.evapp.android.model.Station
import com.evapp.android.model.Vendor
import com.evapp.android.viewmodel.VendorStationAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")
class UserHomeFragment : Fragment(), OnMapReadyCallback {

    private var TAG = "UserHomeFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private var mUserMap: MutableList<Place> = ArrayList()
    private var mStationList: MutableList<Station> = ArrayList()
    lateinit var user_home_recycler_view: RecyclerView

    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        user_home_recycler_view = view.findViewById(R.id.user_home_recycler_view)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fetchUserDetails()
        fetchStation()

        /*mUserMap.add(Place("Branner Hall", "Best dorm at stanford", 37.426, -122.163))
        mUserMap.add(Place("Gates CS Building", "Many long time in basement", 37.430, -122.173))
        mUserMap.add(Place("Pinkberry", "First date with my wife", 37.444, -122.170))*/

        return view

    }//onCreate()

    private fun fetchUserDetails() {
        database.getReference("user").child("account").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mVendor = dataSnapshot.getValue(Vendor::class.java)!!
                    latitude = mVendor.latitude
                    longitude = mVendor.longitude
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchStation() {
        database.getReference("vendor").child("station")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mStationList.clear()
                    mUserMap.clear()

                    for (singleSnapshot in dataSnapshot.children) {
                        for (childSnapshot in singleSnapshot.children) {
                            val mStation = childSnapshot.getValue(Station::class.java)!!
                            mStationList.add(mStation)
                            mUserMap.add(Place(mStation.name, mStation.station_name, mStation.latitude, mStation.longitude))
                        }
                    }

                    user_home_recycler_view.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    user_home_recycler_view.adapter = VendorStationAdapter("home",
                        mStationList, latitude, longitude)
                    (user_home_recycler_view.adapter as VendorStationAdapter).notifyDataSetChanged()

                    // Add a marker in latLng and move the camera
                    val boundsBuilder = LatLngBounds.Builder()
                    for (place in mUserMap) {
                        val latLng = LatLng(place.latitude, place.longitude)
                        boundsBuilder.include(latLng)

                        val drawable = resources.getDrawable(R.drawable.ic_vector) as BitmapDrawable
                        val bitmap = Bitmap.createScaledBitmap(drawable.bitmap, 100, 100, false)

                        mMap.addMarker(MarkerOptions().position(latLng).title(place.title).snippet(place.description)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                    }

                    if (mUserMap.isNotEmpty()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 1000, 1000, 0))
                    }
                    mapView.onResume()

                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

}