package com.example.impressed_1_0

import android.app.AlertDialog
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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import kotlinx.android.synthetic.main.activity_biz_dashboard.*
import kotlinx.android.synthetic.main.activity_customer.customer_heart_countdown_display
import kotlinx.android.synthetic.main.activity_customer.customer_logout
import kotlinx.android.synthetic.main.coupon_amount.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.icon
import kotlinx.android.synthetic.main.new_device_dialog.view.newdevice_cancel
import kotlinx.android.synthetic.main.new_device_dialog.view.newdevice_save
import kotlin.math.roundToInt


// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null


// set sensor vars ends

private var heart_sum = 0
private var heart_life:Int = 0
private var heart_exp_warning:Long = 0L
private var coupon_balance = 0
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

        // reset heart counter
        heart_sum = 0





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



        logged_phone.text = phoneFormat(customer_logged_phone.toString())
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
        if (event != null && event.values[0] < -4) {
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

                for (ds in dataSnapshot.children) {

                    Log.d("text-location",ds.toString())

                    when (ds.key){
                        "locationName" -> store_name.text = ds.getValue(String::class.java)!!
                        "heartWorth"->  customer_heart_display.text = ds.getValue(Long::class.java)!!.toString()
                        "heartLife" -> {
                            heart_life = ds.getValue(Int::class.java)!!
                            customer_heart_life_display.text = heart_life.toString()
                        }


                    }
                }




//                store_name.text = dataSnapshot.child("locationName").getValue().toString()
//                customer_heart_display.text = dataSnapshot.child("heartWorth").getValue().toString()
//                customer_heart_life_display.text = dataSnapshot.child("heartLife").getValue().toString()
//                heart_life = dataSnapshot.child("heartLife").getValue(Int::class.java)!!

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

                // clear out timer

                heart_exp_warning = 0

                for (ds in dataSnapshot.children) {
                    val heart_time = ds.child("time").getValue(Long::class.java)!!
                    val time = System.currentTimeMillis() / 1000L

                    val exp_time = heart_time+(heart_life*86400)


                    // only unexpired is counted
                    if(exp_time>time){
                    heart_sum += ds.child("heartBank").getValue(Int::class.java)!!
                        // only the lowest value get update
                        if(exp_time<heart_exp_warning|| heart_exp_warning == 0L){
                            heart_exp_warning = exp_time

                    }


                    }



                }


                customer_heart_bank.text = heart_sum.toString()
                val time = System.currentTimeMillis() / 1000L
                val time_left = heart_exp_warning-time

                // display time left in hours if less than a day
                if(time_left<86400 && time_left > 0) {

                    val heart_exp = (heart_exp_warning-time)/3600
                    customer_heart_countdown_display.text = heart_exp.toString()
                    heart_time_unit.text = "ชม."
                    heart_life_exp.visibility = View.VISIBLE

                }else {
                    //display time left in days unit

                    val heart_exp = (heart_exp_warning-time)/86400
                    customer_heart_countdown_display.text = heart_exp.toString()
                    heart_life_exp.visibility = View.VISIBLE
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

    // heart_exp database read





    private fun promos_database_read(){

        //read data

        var biz_uid = auth.currentUser!!.uid


        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // coupon database read

                // clean out frame
                customer_promo_frame.removeAllViews()

                for (dsc in dataSnapshot.child("coupons").children) {

                    var promoName = dsc.child("promoName").getValue(String::class.java)
                    var promoWorth = dsc.child("promoWorth").getValue(Int::class.java)
                    var couponLife = dsc.child("couponLife").getValue(Int::class.java)
                    var couponPrice = dsc.child("price").getValue(Float::class.java)
                    var couponKey = dsc.key.toString()



                    val promo_set = LayoutInflater.from(this@customer).inflate(R.layout.promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val couponWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val promoWorth_img_btn = promo_set.findViewById<ImageButton>(R.id.heartButton)
                    promoName_holder.text = promoName+" | "+couponLife+" วัน | "+couponPrice!!.roundToInt().toString()+" บ."
                    promoWorth_img_btn.setEnabled(false)

                    // get couponBalance read database again

                    var coupon_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(customer_logged_phone.toString())

                    val coupon_listener = object : ValueEventListener {


                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            // clear coupon balance

                            coupon_balance = 0


                            for (dscb in dataSnapshot.children) {


                                if(dscb.child("key").getValue(String::class.java).toString() == couponKey) {
                                    val coupon_count =
                                        dscb.child("couponBank").getValue(Int::class.java)!!

                                    // get exp time parameters
                                    val coupon_time =
                                        dscb.child("time").getValue(Long::class.java)!!
                                    val time = System.currentTimeMillis() / 1000L
                                    val exp_time = coupon_time + (couponLife!!* 86400)

                                    if (exp_time > time) {
                                        coupon_balance += coupon_count
                                        val time_left = (exp_time - time)/86400

                                        promoName_holder.text = promoName + " | " + time_left.toString() + " วัน"
                                    }
                                }


                                couponWorth_holder.text = coupon_balance.toString()

                                // check coupon balance and set btn status



                                if(coupon_balance > 0){
                                    promoWorth_img_btn.setBackgroundResource(R.drawable.coupon_btn)
                                    promoWorth_img_btn.setImageResource(R.drawable.g_heart)
                                    promoWorth_img_btn.setEnabled(true)
                                    promoName_holder.setBackgroundResource(R.drawable.coupon_btn)
                                    promoName_holder.setTextColor(getColor(R.color.colorWhite))
                                }

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

                    coupon_ref.addValueEventListener(coupon_listener)



                    promoWorth_img_btn.setOnClickListener{

                        // coupon amount selector

                            //Inflate the dialog with custom view
                            val mDialogView = LayoutInflater.from(this@customer).inflate(R.layout.coupon_amount,null)

                            //AlertDialogBuilder
                            val mBuilder = AlertDialog.Builder(this@customer)
                                .setView(mDialogView)
                                .setTitle("กรุณาเลือกจำนวนหัวใจที่จะใช้")


                            //show dialog
                            val  mAlertDialog = mBuilder.show()

                            // disable touch outside

                            mAlertDialog.setCanceledOnTouchOutside(false)


                            // enable btn if text field is not empty - start

                            mDialogView.coupon_amount.addTextChangedListener(object :
                                TextWatcher {
                                override fun onTextChanged(
                                    s: CharSequence,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) { if (s.toString().trim { it <= ' ' }.length > 0){
                                    mDialogView.newdevice_save.setEnabled(true)
                                    mDialogView.newdevice_save.setBackgroundColor(resources.getColor(R.color.colorBackground))

                                }else{
                                    mDialogView.newdevice_save.setEnabled(false)
                                    mDialogView.newdevice_save.setBackgroundColor(resources.getColor(R.color.colorDisabled))

                                }
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence, start: Int, count: Int,
                                    after: Int
                                ) { // TODO Auto-generated method stub
                                }

                                override fun afterTextChanged(s: Editable) { // TODO Auto-generated method stub
                                }
                            })

                            // enable btn if text field is not empty - ends



                            //login button click of custom layout
                            mDialogView.newdevice_save.setOnClickListener {

                                // send data to customer_redeem

                                val intent = Intent(this@customer,customer_redeem::class.java)
                                intent.putExtra("redeem_name",promoName)
                                intent.putExtra("promoWorth",mDialogView.coupon_amount.text.toString())
                                intent.putExtra("coupon_key",couponKey)
                                startActivity(intent)



                                //dismiss dialog
                                mAlertDialog.dismiss()


                            }
                            //cancel button click of custom layout
                            mDialogView.newdevice_cancel.setOnClickListener {
                                //dismiss dialog
                                mAlertDialog.dismiss()
                            }

                            // dialog with editText ends




                        // coupon amount selector ends




                    }


                    customer_promo_frame.addView(promo_set)



                }


                // add separator
                val line_view = LayoutInflater.from(this@customer).inflate(R.layout.line,null)
                customer_promo_frame.addView(line_view)

                // coupon database read ends





                for (ds in dataSnapshot.child("promos").children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)

                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)



                    val promo_set = LayoutInflater.from(this@customer).inflate(R.layout.promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val promoWorth_img_btn = promo_set.findViewById<ImageButton>(R.id.heartButton)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()

                    // check heart bank and set btn status

                    promoWorth_img_btn.setEnabled(false)

                    if(heart_sum - promoWorth!! >= 0){
//                        val active_color = ContextCompat.getColor(this@customer,R.color.colorHeart)
                        promoWorth_img_btn.setBackgroundResource(R.drawable.heart_btn)
                        promoWorth_img_btn.setEnabled(true)
                        promoName_holder.setBackgroundResource(R.drawable.enabled_border)
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
