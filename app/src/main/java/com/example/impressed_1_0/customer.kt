package com.example.impressed_1_0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.impressed_1_0.MyApplication.Companion.global_main_activity


import kotlinx.android.synthetic.main.activity_customer.*

class customer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        val rotation = windowManager.defaultDisplay.rotation

//        if (global_main_activity == false && rotation == 1){
//            global_main_activity = true
//            startActivity(Intent(this,MainActivity::class.java))
//        }

    store_name.text = "store name"

        // set log out btn
        log_out_btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}
