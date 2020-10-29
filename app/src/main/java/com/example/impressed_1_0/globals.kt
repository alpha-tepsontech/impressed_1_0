package com.example.impressed_1_0

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.name_dialog.view.*
import java.util.*

// start data class
data class Location(
    var locationName: String? = "",
    var heartWorth: Int? = 0
)
// ends

// start data class
data class Devices(
    var deviceID: String? = "",
    var deviceName: String? = "",
    var locationKey:String? = ""
)
// ends

// start data class
data class Promos(
    var PromoName: String? = "",
    var PromoWorth: Int? = 0
)
// ends

// start data class
data class UserData(
    var user_phone: String? = "",
    var status: Int? = 0
)
// ends

// [START User_class]
data class User(
    var phone: String? = "",
    var name: String? = "",
    var loc_key:String? = ""
)
// [END user_class]

// start data class
data class Transaction(
    var phone: String? = "",
    var heartBank: Int? = 0,
    var amount:Float =0.0F,
    var upsale:Float =0.0F,
    var type:String? = "",
    var time: Date = Date()

)
// ends






