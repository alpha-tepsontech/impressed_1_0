package com.example.impressed_1_0

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.impressed_1_0.MyApplication.Companion.global_exp_date
import com.example.impressed_1_0.MyApplication.Companion.global_status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_payment.*




class payment : AppCompatActivity() {


    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
// firebase realtime database setup
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val qr_image = findViewById(R.id.qr_image) as ImageView

        Picasso.get()
            .load("http://www.tepsontech.com/pratupjai/qr_jet.png")
//            .resize(300,200)
            .into(qr_image)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference
        // init ends

        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE


        // set total price

        val total = intent.getStringExtra("total")
        amount.text = total
        val total_clean = total.replace(",","")
        // show exp date time

        val time = System.currentTimeMillis() / 1000L
        val confirm_time = time+(86400*2)
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy 'เวลา 16:00'")
        val date = java.util.Date(confirm_time * 1000)
        val expDateTime = sdf.format(date)

        exp_date_time.text = expDateTime

        // set back btn

        cancelBtn.setOnClickListener{
            finish()
        }

        nextBtn.setOnClickListener{

            var biz_uid = auth.currentUser!!.uid

            //if trial ends set due date then change status
            val time = System.currentTimeMillis() / 1000L
            val exp_time = time+(86400*3)

            var payment_key = database.push().key.toString()

            database.child("biz_owners").child(biz_uid).child("info").child("requested_time_limit").setValue(exp_time)
            database.child("biz_owners").child(biz_uid).child("info").child("payments").child(payment_key).child("expected_amount").setValue(total_clean)
            database.child("biz_owners").child(biz_uid).child("info").child("payments").child(payment_key).child("request_time").setValue(time)

            database.child("biz_owners").child(biz_uid).child("info").child("status").setValue(4)
            //set global status
            global_status = 4

            startActivity(Intent(this@payment,dashboard::class.java))


        }

    }
}