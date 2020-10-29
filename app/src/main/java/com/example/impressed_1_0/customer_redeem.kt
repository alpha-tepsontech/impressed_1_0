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
import android.view.View
import com.firebase.ui.auth.ui.InvisibleActivityBase
import kotlinx.android.synthetic.main.redeem.view.*



// set sensor vars
private var mSensorManager : SensorManager?= null
private var mAccelerometer : Sensor?= null


// set sensor vars ends

class customer_redeem : AppCompatActivity(), SensorEventListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_redeem)




        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // gravity sensor setup ends

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@customer_redeem).inflate(R.layout.redeem,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@customer_redeem)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.redeem_title.text = "พลิกเครื่องให้แคชเชียร์เลยครับ"
        mDialogView.redeem_name.text = intent.getStringExtra("redeem_name")
        mDialogView.redeem_amount.text = intent.getStringExtra("promoWorth")
        mDialogView.redeem_btn.visibility = View.GONE
        //login button click of custom layout

        //cancel button click of custom layout
        mDialogView.redeem_cancel.setOnClickListener {
            //dismiss dialog
            startActivity(Intent(this,customer::class.java))
        }

        // dialog with editText ends



    }

    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] < -4) {

            val redeem_name = intent.getStringExtra("redeem_name")
            val promo_worth = intent.getStringExtra("promoWorth")



            val intent = Intent(this,biz_redeem::class.java)
            intent.putExtra("biz_redeem_name",redeem_name)
            intent.putExtra("biz_promoWorth",promo_worth)
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
