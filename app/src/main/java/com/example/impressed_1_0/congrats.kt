package com.example.impressed_1_0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.global_customers
import com.example.impressed_1_0.MyApplication.Companion.global_sales
import com.example.impressed_1_0.MyApplication.Companion.global_upsales
import kotlinx.android.synthetic.main.activity_choices.*
import kotlinx.android.synthetic.main.activity_congrats.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.redeem.view.*

class congrats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congrats)

        // set up text

        sales_display.text = global_sales.toString()
        upsales_display.text = global_upsales.toString()
        customers_display.text = global_customers.toString()


        // lock back btn

        if(intent.getBooleanExtra("lock",false) == true){

            cancelBtn.setOnClickListener {

                Toast.makeText(baseContext, "กรุณาชำระเงินเพื่อใช้งานต่อ",
                    Toast.LENGTH_SHORT).show()

            }

        }else{
            cancelBtn.setOnClickListener {
                finish()
            }
        }


        nextBtn.setOnClickListener {

            startActivity(Intent(this,choices::class.java))
        }






    }
}