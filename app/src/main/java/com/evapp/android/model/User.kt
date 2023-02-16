package com.evapp.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var uid: String = "", var name: String = "",
                var email: String = "", var password: String = "",
                var area: String = "", var city: String = "",
                var state: String = "", var country: String = "",
                var latitude: Double = 0.0, var longitude: Double = 0.0,
                var postal: String = ""): Parcelable
