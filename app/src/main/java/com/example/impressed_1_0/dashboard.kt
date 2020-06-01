package com.example.impressed_1_0

// set up sensor events

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.forgot_pw_dialog.view.*
import kotlinx.android.synthetic.main.heart_worth_dialog.view.*


// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends

class dashboard : AppCompatActivity() , SensorEventListener {



    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]
// firebase realtime database setup
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference
        // init ends


        // set elements
        // get data from database

        var biz_uid = auth.currentUser!!.uid

        var biz_ref  = database.orderByChild("biz_owners").equalTo(biz_uid)

        val biz_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot !== null) {

                    for (ds in dataSnapshot.children) {
                        val heart_recorded = ds.child("heart_worth").getValue(String::class.java)

                        heart_display.text = heart_recorded

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error", "loadPost:onCancelled", databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Failed to load post.",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }
       biz_ref.addValueEventListener(biz_listener)
        // database ends


        if(auth.currentUser !== null) {
            dash_biz_email.text = auth.currentUser!!.email
        }

        log_out_btn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this,launcher_land::class.java))
        }

        heart_edit.setOnClickListener {

            heart_dialog()
        }


        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        // gravity sensor setup ends

    }
// gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event != null && event.values[0] > 8) {
            startActivity(Intent(this,MainActivity::class.java))

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

    private fun heart_dialog(){

// dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.heart_worth_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.heart_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var heart_worth = mDialogView.dialog_heart_value.text.toString()

            database.child("biz_owners").child(biz_uid).child("heart_worth").setValue(heart_worth)

            //dismiss dialog
            mAlertDialog.dismiss()

        }
        //cancel button click of custom layout
        mDialogView.heart_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }





}
