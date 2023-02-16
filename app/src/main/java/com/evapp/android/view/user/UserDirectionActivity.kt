package com.evapp.android.view.user

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.evapp.android.R
/*import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute*/
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("Lifecycle")
class UserDirectionActivity : AppCompatActivity() {

    /*var mapView: MapView? = null

    private var origin: Point? = null
    private var destination: Point? = null
    private var currentRoute: DirectionsRoute? = null
    private var client: MapboxDirections? = null
    private var navigationMapRoute: NavigationMapRoute? = null

    private val ROUTE_LAYER_ID = "route-layer-id"
    private val ROUTE_SOURCE_ID = "route-source-id"
    private val ICON_LAYER_ID = "icon-layer-id"
    private val ICON_SOURCE_ID = "icon-source-id"
    private val RED_PIN_ICON_ID = "red-pin-icon-id"

    private var user_latitude = 0.0
    private var user_longitude = 0.0
    private var vendor_latitude = 0.0
    private var vendor_longitude = 0.0*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_user_direction)

        /*mapView = findViewById(R.id.mapView)
        mapView!!.onCreate(savedInstanceState)

        user_latitude = intent.getDoubleExtra("user_latitude", 0.0)
        user_longitude = intent.getDoubleExtra("user_longitude", 0.0)
        vendor_latitude = intent.getDoubleExtra("vendor_latitude", 0.0)
        vendor_longitude = intent.getDoubleExtra("vendor_longitude", 0.0)

        mapView!!.getMapAsync {
            it.setStyle(Style.MAPBOX_STREETS) { style ->
                origin = Point.fromLngLat(user_longitude, user_latitude)
                destination = Point.fromLngLat(vendor_longitude, vendor_latitude)

                initSource(style)
                initLayers(style)

                *//*val options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(true)
                    .build()
                NavigationLauncher.startNavigation(this, options)*//*

                // Get the directions route from the Mapbox Directions API
                getRoute(it, origin!!, destination!!)

            }
        }*/

    }//onCreate()

    /*Add the route and marker sources to the map*/
    /*private fun initSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(GeoJsonSource(ROUTE_SOURCE_ID))
        val iconGeoJsonSource = GeoJsonSource(
            ICON_SOURCE_ID, FeatureCollection.fromFeatures(
                arrayOf<Feature>(
                    Feature.fromGeometry(Point.fromLngLat(origin!!.longitude(), origin!!.latitude())),
                    Feature.fromGeometry(Point.fromLngLat(destination!!.longitude(), destination!!.latitude()))
                )
            )
        )
        loadedMapStyle.addSource(iconGeoJsonSource)
    }

    *//*Add the route and marker icon layers to the map*//*
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initLayers(loadedMapStyle: Style) {
        val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            lineCap(Property.LINE_CAP_ROUND),
            lineJoin(Property.LINE_JOIN_ROUND),
            lineWidth(5f),
            lineColor(Color.parseColor("#009688"))
        )
        loadedMapStyle.addLayer(routeLayer)

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(
            RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                resources.getDrawable(R.drawable.ic_baseline_location_on_24))!!
        )

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(
            SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(arrayOf(0f, -9f))
            )
        )
    }

    private fun getRoute(mapboxMap: MapboxMap?, origin: Point, destination: Point) {
        *//*client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .steps(true)
            .accessToken(getString(R.string.access_token))
            .build()

        client!!.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(call: Call<DirectionsResponse?>, response: Response<DirectionsResponse?>) {
                if (response.body() == null) {
                    Toast.makeText(this@UserDirectionActivity, "No routes found, " +
                            "make sure you set the right user and access token.", Toast.LENGTH_SHORT).show()
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Toast.makeText(this@UserDirectionActivity, "No routes found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Get the directions route
                currentRoute = response.body()!!.routes()[0]

                // Make a toast which displays the route's distance
                Toast.makeText(this@UserDirectionActivity, "", Toast.LENGTH_SHORT).show()
                mapboxMap?.getStyle(Style.OnStyleLoaded { style -> // Retrieve and update the source designated for showing the directions route
                    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    source?.setGeoJson(LineString.fromPolyline(currentRoute!!.geometry().toString(), PRECISION_6))
                })
            }

            override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {
                Toast.makeText(this@UserDirectionActivity, "Error: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })*//*

        *//*NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : Callback<DirectionsResponse?> {
                override fun onResponse(call: Call<DirectionsResponse?>,
                    response: Response<DirectionsResponse?>) {
                    if (response.body() != null && response.body()!!.routes().size > 1) {
                        currentRoute = response.body()!!.routes()[0]
                        if (navigationMapRoute != null) {
                            navigationMapRoute!!.removeRoute()
                        } else {
                            navigationMapRoute = NavigationMapRoute(null,
                                mapView!!, mapboxMap!!, com.mapbox.services.android.navigation.ui.v5.R.style.NavigationMapRoute)
                        }
                        navigationMapRoute!!.addRoute(currentRoute)
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse?>, t: Throwable) {

                }
            })*//*
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }*/

}

