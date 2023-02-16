package com.evapp.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Station(var uid: String = "", var name: String = "",
                   var station_id: String = "", var station_name: String = "",
                   var area: String = "", var city: String = "",
                   var state: String = "", var country: String = "",
                   var latitude: Double = 0.0, var longitude: Double = 0.0,
                   var image: String = "", var cost: String = "",
                   var postal: String = "",
                   var port: String = "", var quantity: String = ""): Parcelable
