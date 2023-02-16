package com.evapp.android.view.user

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.evapp.android.viewmodel.ApiInterface
import com.evapp.android.viewmodel.Route
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import com.evapp.android.R
import com.evapp.android.viewmodel.GoogleMapDTO
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class UserNavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    /*private lateinit var map: GoogleMap
    private lateinit var apiInterface: ApiInterface
    private lateinit var polylineList: List<LatLng>
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var orig : LatLng
    private lateinit var dest : LatLng*/

    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

    private var user_latitude = 0.0
    private var user_longitude = 0.0
    private var vendor_latitude = 0.0
    private var vendor_longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_navigation)

        user_latitude = intent.getDoubleExtra("user_latitude", 0.0)
        user_longitude = intent.getDoubleExtra("user_longitude", 0.0)
        vendor_latitude = intent.getDoubleExtra("vendor_latitude", 0.0)
        vendor_longitude = intent.getDoubleExtra("vendor_longitude", 0.0)

        mapView = findViewById(R.id.user_navigation_map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        mapView.onResume()

        /*val mapFragment = supportFragmentManager.findFragmentById(R.id.user_navigation_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("https://maps.googleapis.com/")
            .build()
        apiInterface = retrofit.create(ApiInterface::class.java)*/

    }//onCreate()

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location1 = LatLng(user_latitude, user_longitude)
        googleMap.addMarker(MarkerOptions().position(location1).title("My Location"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1, 5f))

        val location2 = LatLng(vendor_latitude, vendor_longitude)
        googleMap.addMarker(MarkerOptions().position(location2).title("Station's"))

        val URL = getDirectionURL(location1, location2)
        GetDirection(URL).execute()

        /*map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.isTrafficEnabled = true

        orig = LatLng(user_latitude, user_longitude)
        dest = LatLng(vendor_latitude, vendor_longitude)

        getDirection("$user_latitude,$user_longitude", "$vendor_latitude,$vendor_longitude")*/
    }

    private fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving"
    }

    private inner class GetDirection(val url: String) : AsyncTask<Void, Void, List<List<LatLng>>>() {

        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()

                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineOption = PolylineOptions()
            for (i in result.indices) {
                lineOption.addAll(result[i])
                lineOption.width(10f)
                lineOption.color(ContextCompat.getColor(applicationContext, R.color.colorBlue))
                lineOption.jointType(JointType.ROUND)
                lineOption.geodesic(true)
            }
            mMap.addPolyline(lineOption)
        }
    }

    /*private fun getDirection(origin: String, destination: String) {
        apiInterface.getDirection("driving", "less_driving", origin, destination,
            getString(R.string.api_key))!!.subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<com.evapp.android.viewmodel.Result?> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onSuccess(r: com.evapp.android.viewmodel.Result?) {
                    polylineList = ArrayList()
                    val routeList: List<Route> = r!!.routes!!
                    for (route in routeList) {
                        val polyline: String? = route.overviewPolyline!!.points
                        (polylineList as ArrayList<LatLng>).addAll(decodePolyline(polyline!!))
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions.color(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                    polylineOptions.width(8F)
                    polylineOptions.startCap(ButtCap())
                    polylineOptions.jointType(JointType.ROUND)
                    polylineOptions.addAll(polylineList)
                    map.addPolyline(polylineOptions)

                    val builder = LatLngBounds.Builder()
                    builder.include(orig)
                    builder.include(dest)
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
                }

                override fun onError(e: Throwable?) {

                }
            })
    }*/

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

}