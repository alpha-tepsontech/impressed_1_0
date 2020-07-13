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
import android.media.Image
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_customer.customer_logout




// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null


// set sensor vars ends

private var heart_sum = 0

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


//        val phone_striped = customer_logged_phone!!.drop(3)
//        val phone_format = DecimalFormat("###,###,####")
//        val formatted_phone = phone_format.format(phone_striped)







        logged_phone.text = customer_logged_phone
        logged_name.text = customer_logged_name

        // set log out btn
        customer_logout.setOnClickListener {

            customer_logged_name = ""
            customer_logged_phone = ""

            unlink("phone")

            startActivity(Intent(this,MainActivity::class.java))

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

        var transaction_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(customer_logged_phone)

        val transaction_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    heart_sum += ds.child("heartBank").getValue(Int::class.java)!!
                    customer_heart_bank.text = heart_sum.toString()
                }

                promos_database_read()

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

    private fun promos_database_read(){

        //read data

        var biz_uid = auth.currentUser!!.uid

        Log.d("test",biz_uid.toString())

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("promos").orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)
                    Log.d("test",promoName)
                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)



                    val promo_set = LayoutInflater.from(this@customer).inflate(R.layout.promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val promoWorth_img_btn = promo_set.findViewById<ImageButton>(R.id.heartButton)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()

                    // check heart bank and set btn status

                    promoWorth_img_btn.setEnabled(false)

                    if(heart_sum - promoWorth!! > 0){
                        val active_color = ContextCompat.getColor(this@customer,R.color.colorHeart)
                        promoWorth_img_btn.setBackgroundColor(active_color)
                        promoWorth_img_btn.setEnabled(true)
                    }

                    promoWorth_img_btn.setOnClickListener{

                        val intent = Intent(this@customer,customer_redeem::class.java)
                        intent.putExtra("redeem_name",promoName)
                        intent.putExtra("promoWorth",promoWorth.toString())
                        startActivity(intent)


                    }





                    customer_promo_frame.addView(promo_set)


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

        promos_ref.addListenerForSingleValueEvent(promos_listener)
        // database ends




    }

    private fun unlink(providerId: String) {

        // [START auth_unlink]
        auth.currentUser!!.unlink(providerId)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                }
            }
        // [END auth_unlink]
    }

}
