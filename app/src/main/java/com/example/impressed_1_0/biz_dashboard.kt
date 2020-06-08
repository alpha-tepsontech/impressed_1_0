package com.example.impressed_1_0

import android.app.Application
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
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_name
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_biz_dashboard.biz_location_display
import kotlinx.android.synthetic.main.activity_biz_dashboard.log_out_btn
import kotlinx.android.synthetic.main.activity_customer.*

// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends





class biz_dashboard : AppCompatActivity() , SensorEventListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]

    // firebase realtime database setup
    private lateinit var database: DatabaseReference


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
            biz_email_display.text = auth.currentUser!!.email
        }

        customer_phone.text = customer_logged_phone
        customer_name.text = customer_logged_name

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends


        // set elements
        // get device data from database

        var biz_uid = auth.currentUser!!.uid

        var device_ref  = database.child("biz_owners").child(biz_uid).child("devices").orderByChild("locationKey").equalTo(global_location_key)

        val device_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                for (ds in dataSnapshot.children) {
                    val deviceName = ds.child("deviceName").getValue()
                    biz_device_display.text = deviceName.toString()
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
        device_ref.addValueEventListener(device_listener)
        // database ends


        // get location data from database

        var location_ref  = database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString())

        val location_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val location_info = dataSnapshot.getValue()

                Log.d("test2",location_info.toString())


                biz_location_display.text = dataSnapshot.child("locationName").getValue().toString()
                heart_display.text = dataSnapshot.child("heartWorth").getValue().toString()

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
        location_ref.addValueEventListener(location_listener)
        // database ends














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

        clear.setOnClickListener {
            //set text
            total_input.setText("")



        }

        total_ent.setOnClickListener {


            Log.d("test","longClicked")


        }

        // custom keyboard ends

        log_out_btn.setOnClickListener {

              customer_logged_phone = ""
              customer_logged_name = ""
            startActivity(Intent(this,dashboard::class.java))
//            auth.signOut()
//            startActivity(Intent(this,launcher_land::class.java))
        }


    }
    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] > 8) {
            startActivity(Intent(this,customer::class.java))


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
