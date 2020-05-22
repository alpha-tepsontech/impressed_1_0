package com.example.impressed_1_0

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_biz_dashboard.*

// set up sensor events
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.Sensor
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends



class biz_dashboard : AppCompatActivity() , SensorEventListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_dashboard)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        // gravity sensor setup ends

        // set elements

        if(auth.currentUser !== null){
            biz_email.text = auth.currentUser!!.email
        }

        // custom keyboard start
        // disable softkeyboard on focus

        total_input.setShowSoftInputOnFocus(false);

        // diasble softkeyboard on focus ends


        letter9.setOnClickListener {

            total_input.append("9")

        }
        letter8.setOnClickListener {

            total_input.append("8")

        }
        letter7.setOnClickListener {

            total_input.append("7")

        }
        letter6.setOnClickListener {

            total_input.append("6")

        }
        letter5.setOnClickListener {

            total_input.append("5")

        }

        letter4.setOnClickListener {

            total_input.append("4")

        }
        letter3.setOnClickListener {

            total_input.append("3")

        }
        letter2.setOnClickListener {

            total_input.append("2")

        }
        letter1.setOnClickListener {

            total_input.append("1")

        }
        letter0.setOnClickListener {

            total_input.append("0")

        }
        decimal.setOnClickListener {

            total_input.append(".")

        }

        total_del.setOnClickListener {
            //delete last digit
            var droped = total_input.text.dropLast(1)
            //set text
            total_input.setText(droped)
            //put cursor to the end
            total_input.setSelection(total_input.text.length);



        }

        total_ent.setOnClickListener {


            Log.d("test","longClicked")


        }

        // custom keyboard ends

        log_out_btn.setOnClickListener {

            auth.signOut()
            startActivity(Intent(this,launcher_land::class.java))
        }


    }

    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] > 8 && MyApplication.global_main_activity == false) {
            MyApplication.global_main_activity = true
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

}
