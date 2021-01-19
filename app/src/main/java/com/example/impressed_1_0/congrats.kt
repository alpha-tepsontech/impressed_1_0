package com.example.impressed_1_0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choices.*
import kotlinx.android.synthetic.main.activity_choices.nextBtn
import kotlinx.android.synthetic.main.activity_congrats.*
import kotlinx.android.synthetic.main.redeem.view.*

class congrats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congrats)

        // set up text

        sales_display.text = intent.getStringExtra("sales")
        upsales_display.text = intent.getStringExtra("upsales")
        customers_display.text = intent.getStringExtra("customers")




        nextBtn.setOnClickListener {

            startActivity(Intent(this,choices::class.java))
        }

        cancelBtn.setOnClickListener {

            finish()
        }




    }
}