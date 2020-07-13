package com.example.impressed_1_0

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.redeem.view.*

// set sensor vars
private var mSensorManager : SensorManager?= null
private var mAccelerometer : Sensor?= null



// set sensor vars ends

class biz_redeem : AppCompatActivity(), SensorEventListener {

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_redeem)

        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        // gravity sensor setup ends

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends




        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_redeem).inflate(R.layout.redeem,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_redeem)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.redeem_title.text = "ยืนยันใช้ promotion"
        mDialogView.redeem_name.text = intent.getStringExtra("biz_redeem_name")
        mDialogView.redeem_amount.text = intent.getStringExtra("biz_promoWorth")
        mDialogView.redeem_btn.text = "ยืนยัน"
        //login button click of custom layout

        //cancel button click of custom layout
        mDialogView.redeem_btn.setOnClickListener {

            // write tx to database
            val heartInsert = -intent.getStringExtra("biz_promoWorth").toInt()

            val transaction_insert = Transaction(customer_logged_phone,heartInsert,0F)

            database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)





            //dismiss dialog
            startActivity(Intent(this,biz_dashboard::class.java))
        }

        // dialog with editText ends



    }

    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] > 8) {
            val intent = Intent(this,customer_redeem::class.java)
            intent.putExtra("redeem_name",intent.getStringExtra("redeem_name"))
            intent.putExtra("promoWorth",intent.getStringExtra("promoWorth"))
            startActivity(intent)

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



}
