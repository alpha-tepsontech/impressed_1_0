package com.example.impressed_1_0

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_name
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone



import kotlinx.android.synthetic.main.activity_customer.*

// set up sensor events
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.Sensor
import android.util.Log
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_customer.log_out_btn


// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends


class customer : AppCompatActivity(), SensorEventListener {


    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
// firebase realtime database setup
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference
        // init ends



        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // gravity sensor setup ends

        // read location info

        location_database_read()

        transaction_database_read()

        logged_phone.text = customer_logged_phone
        logged_name.text = customer_logged_name

        // set log out btn
        log_out_btn.setOnClickListener {
            customer_logged_name = ""
            customer_logged_phone = ""
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
// gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] < -8) {
            startActivity(Intent(this,biz_dashboard::class.java))

        }

    }
    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this,mAccelerometer,
            SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    // gravity sensor code ends


    private fun location_database_read(){


        var biz_uid = auth.currentUser!!.uid

        var location_ref  = database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString())

        val location_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var read = dataSnapshot.getValue()
                Log.d("test",read.toString())
                store_name.text = dataSnapshot.child("locationName").getValue().toString()
                customer_heart_display.text = dataSnapshot.child("heartWorth").getValue().toString()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        location_ref.addListenerForSingleValueEvent(location_listener)
        // database ends

    }

    private fun transaction_database_read(){

        var transaction_ref  = database.child("transactions").orderByChild("phone").equalTo(customer_logged_phone)

        val transaction_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    var heartReceived = ds.child("heartReceived").getValue(Int::class.java)
                    customer_heart_bank.text = heartReceived.toString()
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        transaction_ref.addListenerForSingleValueEvent(transaction_listener)
        // database ends


    }


}
