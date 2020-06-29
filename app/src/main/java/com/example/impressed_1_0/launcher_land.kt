package com.example.impressed_1_0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_launcher_land.*
import java.security.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class launcher_land : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher_land)

        launch_land_logo.setOnLongClickListener {

            startActivity(Intent(this, biz_auth::class.java))

            true
        }


        //send to activity base on rotation

        val rotation = windowManager.defaultDisplay.rotation

        if (rotation == 0 || rotation == 2){

            startActivity(Intent(this, launcher::class.java))
        }

    }
}
