package com.evapp.android.model

import android.os.Parcelable
import com.google.firebase.database.ServerValue
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Booking(var booking_id: String = "",
                   var user_id: String = "", var vendor_id: String = "",
                   var station_id: String = "", var user_name: String = "",
                   var vendor_name: String = "", var station_name: String = "",
                   var area: String = "", var city: String = "",
                   var state: String = "", var country: String = "",
                   var postal: String = "", var latitude: Double = 0.0,
                   var longitude: Double = 0.0, var charging_cost: String = "",
                   var total_cost: String = "", var vehicle_company: String = "",
                   var vehicle_model: String = "", var slot_time: String = "",
                   var kilometer: String = "",
                   var status: String = "", val timestamp :@RawValue Any = ServerValue.TIMESTAMP): Parcelable
