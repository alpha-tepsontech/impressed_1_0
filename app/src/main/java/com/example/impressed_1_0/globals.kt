package com.example.impressed_1_0

import android.app.AlertDialog
import android.app.Application
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
    var status: Int? = 0,
    var date_joined: Long  = System.currentTimeMillis() / 1000L
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
    var time: Long = System.currentTimeMillis() / 1000L

)
// ends


// set global val

class MyApplication: Application() {
    companion object {
        var customer_logged_name : String? = ""
        var customer_logged_phone: String? = ""
        var global_location_key:String? =""
        var global_device_id:String? = ""
        var global_device_key:String? = ""
        var global_status:Int? = 1

    }

}
// set global val ends

// set global fun

fun phoneFormat(rawPhone:String): String {

    val rawPhone_drop = rawPhone.drop(3)

    return "("+ rawPhone_drop.substring(0, 3) + ")-" + rawPhone_drop.substring(3,6) +"-"+ rawPhone_drop.substring(6);



}



