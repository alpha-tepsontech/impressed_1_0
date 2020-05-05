package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import kotlinx.android.synthetic.main.activity_customer.*

class customer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

    store_name.text = "store name"



    }
}
