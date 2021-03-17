package com.example.impressed_1_0

// set up sensor events

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_customers
import com.example.impressed_1_0.MyApplication.Companion.global_device_id
import com.example.impressed_1_0.MyApplication.Companion.global_device_key
import com.example.impressed_1_0.MyApplication.Companion.global_exp_date
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.example.impressed_1_0.MyApplication.Companion.global_sales
import com.example.impressed_1_0.MyApplication.Companion.global_status
import com.example.impressed_1_0.MyApplication.Companion.global_upsales
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_biz_auth.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.view.*
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.dialog_frame.view.*
import kotlinx.android.synthetic.main.email_update.view.*
import kotlinx.android.synthetic.main.heart_worth_dialog.view.*
import kotlinx.android.synthetic.main.new_coupon_dialog.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.*
import kotlinx.android.synthetic.main.new_loc_dialog.view.*
import kotlinx.android.synthetic.main.new_promo_dialog.view.*
import kotlinx.android.synthetic.main.new_promo_dialog.view.dialogAddBtn
import kotlinx.android.synthetic.main.new_promo_dialog.view.dialogCancelBtn
import kotlinx.android.synthetic.main.new_promo_dialog.view.new_promo_name
import kotlinx.android.synthetic.main.new_promo_dialog.view.promo_worth
import kotlinx.android.synthetic.main.promo_del_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.radio_set.*
import kotlinx.android.synthetic.main.reauth.*
import kotlinx.android.synthetic.main.reauth.view.*
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.time.ZoneId
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

// set elements vars
private var heart_sum = 0
private var sales_sum:Float = 0f
private var upsales_sum:Float = 0f
private var total_customer_sum:Int = 0

// set treshold vars

private var sales_treshold:Float = 0f
private var upsales_treshold:Float = 0f
private var customers_treshold:Int = 0

// set exp_time vars

private var db_exp_time:Long = 0
private var db_requested_time_limit:Long = 0

// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends

//// start data class
//data class Promos(
//    var PromoName: String? = "",
//    var PromoWorth: Int? = 0
//)
//// ends

class dashboard : AppCompatActivity() , SensorEventListener {



    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]
// firebase realtime database setup
    private lateinit var database: DatabaseReference

//    var loc_key = global_location_key.toString()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // underline paynow button.

        paynow.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        // set onclick for paynow button
        paynow.setOnClickListener{
            startActivity(Intent(this,choices::class.java))
        }

        paynow2.setOnClickListener{
            startActivity(Intent(this,choices::class.java))
        }


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference
        // init ends

        // show progress bar
        dash_progress.visibility = View.VISIBLE


        // call device_read to get all the elements from the database
        device_database_read()






        if(auth.currentUser !== null) {
            dash_biz_email.text = auth.currentUser!!.email
        }

        log_out_btn.setOnClickListener {
            unlink("phone")
            auth.signOut()
            startActivity(Intent(this,launcher_land::class.java))
        }

        heart_edit.setOnClickListener {

            heart_dialog()
        }

        location_edit.setOnClickListener {
            location_edit()
        }
        location_select.setOnClickListener {
            location_select()
        }
        new_loc.setOnClickListener {
            new_loc_dialog()
        }

        device_edit.setOnClickListener {
            new_device_dialog()
        }

        // biz elements set up

        newPromoBtn.setOnClickListener{

            new_promo_selector()

        }

        email_edit.setOnClickListener{

            email_edit()
        }

        del_loc.setOnClickListener {
            location_del()
        }

        device_del.setOnClickListener {
            device_del()
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

    private fun device_database_read(){


        // get device data from database

        var biz_uid = auth.currentUser!!.uid

        val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        var device_ref  = database.child("biz_owners").child(biz_uid).child("devices").orderByChild("deviceID").equalTo(deviceID)

        val device_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

               // clear out previous value
                global_device_id = null


                for (ds in dataSnapshot.children) {
                    val location_key = ds.child("locationKey").getValue()
                    global_location_key = location_key.toString()
                    val deviceName = ds.child("deviceName").getValue()
                    device_display.text = deviceName.toString()
                    val database_deviceID = ds.child("deviceID").getValue()
                    global_device_id = database_deviceID.toString()
                    val deviceKey = ds.key
                    global_device_key = deviceKey.toString()
                }


                // check for a new device

                Log.d("test-global", global_device_id.toString())
                Log.d("test-ID",deviceID.toString())
                if (global_device_id != deviceID || global_device_id == null){

                    Log.d("test","if executed")

                    add_new_device()

                }

                // call location_read after key is obtained

                location_database_read()
                promos_database_read()
//                coupons_database_read()
                transaction_database_read()

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

    }





    private fun location_database_read(){


        var biz_uid = auth.currentUser!!.uid

        var location_ref  = database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString())

        val location_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                location_display.text = dataSnapshot.child("locationName").getValue().toString()
                heart_display.text = dataSnapshot.child("heartWorth").getValue().toString()
                heart_life_display.text = dataSnapshot.child("heartLife").getValue().toString()

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

        location_ref.addValueEventListener(location_listener)
        // database ends

    }



// gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event != null && event.values[0] > 4) {
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
            .setTitle("ใส่มูลค่าหัวใจและวันหมดอายุ")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        // enable btn if text field is not empty - start

        mDialogView.dialog_heart_value.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { if (s.toString().trim { it <= ' ' }.length > 0){
                mDialogView.dialog_heart_life.setEnabled(true)
                mDialogView.dialog_heart_life.backgroundTintList = ColorStateList.valueOf(R.color.colorBackground)


            }else{
                mDialogView.dialog_heart_life.setEnabled(false)
                mDialogView.dialog_heart_life.backgroundTintList = ColorStateList.valueOf(R.color.colorLightGray)


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


        // enable btn if text field is not empty - start

        mDialogView.dialog_heart_life.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { if (s.toString().trim { it <= ' ' }.length > 0){
                mDialogView.heart_save.setEnabled(true)
                mDialogView.heart_save.setBackgroundColor(resources.getColor(R.color.colorBackground))

            }else{
                mDialogView.heart_save.setEnabled(false)
                mDialogView.heart_save.setBackgroundColor(resources.getColor(R.color.colorDisabled))

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
        mDialogView.heart_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var heart_worth = mDialogView.dialog_heart_value.text.toString()

            var heart_life = mDialogView.dialog_heart_life.text.toString()

            database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString()).child("heartWorth").setValue(heart_worth)
            database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString()).child("heartLife").setValue(heart_life)

            //dismiss dialog
            mAlertDialog.dismiss()

        }
        //cancel button click of custom layout
        mDialogView.heart_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }


    }

    private fun new_loc_dialog(){


        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_loc_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        // enable btn if text field is not empty - start

        mDialogView.dialog_newlocation.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { if (s.toString().trim { it <= ' ' }.length > 0){
                mDialogView.dialog_heart_worth.setEnabled(true)
                mDialogView.dialog_heart_worth.backgroundTintList = ColorStateList.valueOf(R.color.colorBackground)



            }else{
                mDialogView.dialog_heart_worth.setEnabled(false)
                mDialogView.dialog_heart_worth.backgroundTintList = ColorStateList.valueOf(R.color.colorLightGray)
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


        // enable btn if text field is not empty - start

        mDialogView.dialog_heart_worth.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { if (s.toString().trim { it <= ' ' }.length > 0){
                mDialogView.newloc_save.setEnabled(true)
                mDialogView.newloc_save.setBackgroundColor(resources.getColor(R.color.colorBackground))



            }else{
                mDialogView.newloc_save.setEnabled(false)
                mDialogView.newloc_save.setBackgroundColor(resources.getColor(R.color.colorDisabled))
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
        mDialogView.newloc_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var new_loc = mDialogView.dialog_newlocation.text.toString()

            var new_heartWorth = mDialogView.dialog_heart_worth.text.toString().toInt()

            var location_key = database.push().key.toString()

            var location_info = Location(new_loc,new_heartWorth)

            database.child("biz_owners").child(biz_uid).child("locations").child(location_key).setValue(location_info)

            global_location_key = location_key

            // get startdard promos

            // read setting from database

            var settings_ref  = database.child("settings")



            val device_listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    for (ds in dataSnapshot.child("standard_promo").children) {
                        val standard_promo_key = ds.key

                        val standard_promo_price: Int? = ds.getValue(Int::class.java)
                        var promo_key = database.push().key.toString()

                        var promo_insert = Promos(standard_promo_key,standard_promo_price)

                        database.child("biz_owners").child(biz_uid).child(location_key).child("promos").child(promo_key).setValue(promo_insert)

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {

                    Toast.makeText(baseContext, "Database Failed.",
                        Toast.LENGTH_SHORT).show()
                    // [END_EXCLUDE]
                }
            }


            settings_ref.addListenerForSingleValueEvent(device_listener)

            //end of setting database read


            //dismiss dialog
            mAlertDialog.dismiss()

            location_select()

        }
        //cancel button click of custom layout
        mDialogView.newloc_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }


    private fun new_device_dialog(){



        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_device_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        // enable btn if text field is not empty - start

        mDialogView.dialog_newdevice.addTextChangedListener(object : TextWatcher {
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

            var biz_uid = auth.currentUser!!.uid

            var new_device_name = mDialogView.dialog_newdevice.text.toString()

            var currentDeviceID  = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            // check if same device
            if (global_device_id == currentDeviceID){

                database.child("biz_owners").child(biz_uid).child("devices").child(global_device_key.toString()).child("deviceName").setValue(new_device_name)
            }else{

                // TODO

            }



            //dismiss dialog
            mAlertDialog.dismiss()

        }
        //cancel button click of custom layout
        mDialogView.newdevice_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }


    private fun promos_database_read(){


        //read data

        var biz_uid = auth.currentUser!!.uid

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // clean out frame
                promo_frame.removeAllViews()


                for (ds in dataSnapshot.child("promos").children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)

                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)

                    var promoKey = ds.key.toString()




                    val btn_set = LayoutInflater.from(this@dashboard).inflate(R.layout.promo_btn_set,null)
                    val promoName_holder = btn_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = btn_set.findViewById<TextView>(R.id.promoWorth_textview)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()
                    promo_frame.addView(btn_set)

                    val promo_del_btn = btn_set.findViewById<ImageButton>(R.id.promo_del_btn)

                        promo_del_btn.setOnClickListener{

                            promo_delete(promoKey,promoName.toString())


                    }
                    val promo_edit_btn = btn_set.findViewById<ImageButton>(R.id.promo_edit_btn)

                    promo_edit_btn.setOnClickListener{

                        promo_edit(promoKey,promoName.toString(),promoWorth)


                    }



                    }

                val line_view = LayoutInflater.from(this@dashboard).inflate(R.layout.line,null)
                promo_frame.addView(line_view)


                for (dsc in dataSnapshot.child("coupons").children) {

                    if(dataSnapshot.child("coupons").childrenCount<0){

                       promo_frame.removeView(line_view)

                    }

                    var promoName = dsc.child("promoName").getValue(String::class.java)

                    var promoWorth = dsc.child("promoWorth").getValue(Int::class.java)

                    var promoKey = dsc.key.toString()

                    var couponLife = dsc.child("couponLife").getValue(Int::class.java)

                    var couponPrice = dsc.child("price").getValue(Float::class.java)




                    val btn_set = LayoutInflater.from(this@dashboard).inflate(R.layout.coupon_btn_set,null)
                    val promoName_holder = btn_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = btn_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val couponLife_holder = btn_set.findViewById<TextView>(R.id.coupon_life_display)
                    val couponPrice_holder = btn_set.findViewById<TextView>(R.id.coupon_price_display)

                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()
                    couponLife_holder.text = couponLife.toString()
                    couponPrice_holder.text = couponPrice!!.roundToInt().toString()
                    promo_frame.addView(btn_set)

                    val promo_del_btn = btn_set.findViewById<ImageButton>(R.id.promo_del_btn)

                    promo_del_btn.setOnClickListener{

                        coupon_delete(promoKey,promoName.toString())


                    }
                    val promo_edit_btn = btn_set.findViewById<ImageButton>(R.id.promo_edit_btn)

                    promo_edit_btn.setOnClickListener{


                        coupon_edit(promoKey,promoName.toString(),promoWorth,couponLife,couponPrice)


                    }



                }
//                val line_view = LayoutInflater.from(this@dashboard).inflate(R.layout.line,null)
//                promo_frame.addView(line_view)

                dash_progress.visibility = View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
                dash_progress.visibility = View.INVISIBLE
            }
        }

        promos_ref.addValueEventListener(promos_listener)
        // database ends

    }

//    private fun coupons_database_read(){
//
//
//
//        //read data
//
//        var biz_uid = auth.currentUser!!.uid
//
//        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("coupons").orderByChild("price")
//
//        val promos_listener = object : ValueEventListener {
//
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//
//                for (ds in dataSnapshot.children) {
//
//                    // get seperation line
////
////                    if (dataSnapshot.childrenCount > 0 ){
////
////                        line.visibility = View.VISIBLE
////
////                    }
//
//                    var promoName = ds.child("promoName").getValue(String::class.java)
//
//                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)
//
//                    var promoKey = ds.key.toString()
//
//                    var couponLife = ds.child("couponLife").getValue(Int::class.java)
//
//                    var couponPrice = ds.child("price").getValue(Float::class.java)
//
//
//
//
//                    val btn_set = LayoutInflater.from(this@dashboard).inflate(R.layout.coupon_btn_set,null)
//                    val promoName_holder = btn_set.findViewById<TextView>(R.id.promoName_textview)
//                    val promoWorth_holder = btn_set.findViewById<TextView>(R.id.promoWorth_textview)
//                    promoName_holder.text = promoName
//                    promoWorth_holder.text = promoWorth.toString()
//                    promo_frame.addView(btn_set)
//
//                    val promo_del_btn = btn_set.findViewById<ImageButton>(R.id.promo_del_btn)
//
//                    promo_del_btn.setOnClickListener{
//
//                        coupon_delete(promoKey,promoName.toString())
//
//
//                    }
//                    val promo_edit_btn = btn_set.findViewById<ImageButton>(R.id.promo_edit_btn)
//
//                    promo_edit_btn.setOnClickListener{
//
//                        coupon_edit(promoKey,promoName.toString(),promoWorth,couponLife,couponPrice)
//
//
//                    }
//
//
//
//                }
//
//
//                dash_progress.visibility = View.INVISIBLE
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("error",databaseError.toException())
//                // [START_EXCLUDE]
//                Toast.makeText(baseContext, "database failed",
//                    Toast.LENGTH_SHORT).show()
//                // [END_EXCLUDE]
//                dash_progress.visibility = View.INVISIBLE
//            }
//        }
//
//        promos_ref.addValueEventListener(promos_listener)
//        // database ends
//
//    }


    private fun new_promo_selector(){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("เลือกชนิดโปรโมชั่นใหม่")
            .setCancelable(false)
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        val radioButton_promo = RadioButton(this@dashboard)


                    radioButton_promo.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton_promo.setText("โปรโมชั่นสะสมหัวใจ")
                    radioButton_promo.id = 0
                    radioButton_promo.tag = "promo"
                    radioButton_promo.isChecked = true
        mDialogView.radioSet_group.addView(radioButton_promo)


        val radioButton_coupon = RadioButton(this@dashboard)


        radioButton_coupon.layoutParams= LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        radioButton_coupon.setText("โปรโมชั่นบัตรคูปอง")
        radioButton_coupon.id = 1
        radioButton_coupon.tag = "coupon"
        mDialogView.radioSet_group.addView(radioButton_coupon)


        //next btn
        mDialogView.frame_next.setOnClickListener {

            // get id from radio group

            var id: Int = mDialogView.radioSet_group.checkedRadioButtonId


            when(id){

                0->write_new_promo()
                1->write_new_coupon()

            }

            mAlertDialog.dismiss()

        }

        mDialogView.frame_cancel.setOnClickListener {

            mAlertDialog.dismiss()

            }













        // dialog with editText ends




    }


    private fun write_new_promo(){

            // dialog with edittext start
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_promo_dialog,null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this@dashboard)
                .setView(mDialogView)
                .setTitle("โปรโมชั่นสะสมหัวใจ")
            //show dialog
            val  mAlertDialog = mBuilder.show()

            // disable touch outside

            mAlertDialog.setCanceledOnTouchOutside(false)


            // check null




                // button click of custom layout
                mDialogView.dialogAddBtn.setOnClickListener {

                    var promoName = mDialogView.new_promo_name.text.toString()
                    var promoWorth = mDialogView.promo_worth.text.toString()

                   if (promoName.isNotEmpty() && promoWorth.isNotEmpty()) {

                       var biz_uid = auth.currentUser!!.uid



                       var promo_insert = Promos(promoName, promoWorth.toInt())
                       database.child("biz_owners").child(biz_uid)
                           .child(global_location_key.toString()).child("promos").push()
                           .setValue(promo_insert)

                       //dismiss dialog
                       mAlertDialog.dismiss()
                       // restart activity
//                       finish();
//                       startActivity(getIntent());

                   } else {

                        Toast.makeText(
                            baseContext, "ใส่ข้อมูลไม่ครบครับ",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

            //cancel button click of custom layout
            mDialogView.dialogCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

            // dialog with editText ends

        }

    private fun promo_delete(promoKey:String,promoName:String){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.promo_del_confirmation_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.promo_del_name.text = promoName
        //login button click of custom layout
        mDialogView.del_conf_DelBtn.setOnClickListener {

            // delete from database

            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("promos").child(promoKey).removeValue()


            //dismiss dialog
            mAlertDialog.dismiss()

            //restart activity
//            finish();
//            startActivity(getIntent());

        }
        //cancel button click of custom layout
        mDialogView.del_conf_CancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }

        private fun promo_edit(promoKey:String, promoName: String, promoWorth: Int?){


                    // dialog with edittext start
                    //Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_promo_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@dashboard)
                        .setView(mDialogView)
                        .setTitle("แก้ไข promotion")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()

                    // disable touch outside

                    mAlertDialog.setCanceledOnTouchOutside(false)

                    // populate edit texts

                    mDialogView.new_promo_name.setText(promoName)

                    mDialogView.promo_worth.setText(promoWorth.toString())




                    // button click of custom layout
                    mDialogView.dialogAddBtn.setOnClickListener {

                        var promoName = mDialogView.new_promo_name.text.toString()
                        var promoWorth = mDialogView.promo_worth.text.toString()

                        if (promoName.isNotEmpty() && promoWorth.isNotEmpty()) {

                            var biz_uid = auth.currentUser!!.uid



                            var promo_insert = Promos(promoName, promoWorth.toInt())
                            database.child("biz_owners").child(biz_uid)
                                .child(global_location_key.toString()).child("promos").child(promoKey).setValue(promo_insert)

                            //dismiss dialog
                            mAlertDialog.dismiss()
                            // restart activity
//                            finish();
//                            startActivity(getIntent());

                        } else {

                            Toast.makeText(
                                baseContext, "ใส่ข้อมูลไม่ครบครับ",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                    //cancel button click of custom layout
                    mDialogView.dialogCancelBtn.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }

                    // dialog with editText ends


                }

    // coupon del and edit functions




    private fun coupon_edit(promoKey:String, promoName: String, promoWorth: Int?,couponLife:Int?,couponPrice:Float?){


        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_coupon_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("แก้ไข coupon")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        // populate edit texts

        mDialogView.new_promo_name.setText(promoName)

        mDialogView.promo_worth.setText(promoWorth.toString())

        mDialogView.coupon_life.setText(couponLife.toString())

        mDialogView.coupon_price.setText(couponPrice.toString())




        // button click of custom layout
        mDialogView.dialogAddBtn.setOnClickListener {

            var promoName = mDialogView.new_promo_name.text.toString()
            var promoWorth = mDialogView.promo_worth.text.toString()
            var couponLife = mDialogView.coupon_life.text.toString()
            var couponPrice = mDialogView.coupon_price.text.toString()
//            var couponTerm = mDialogView.coupon_term.text.toString()

            if (promoName.isNotEmpty() && promoWorth.isNotEmpty()&& couponLife.isNotEmpty()&&couponPrice.isNotEmpty()) {

                var biz_uid = auth.currentUser!!.uid



                var coupon_insert = Coupons(promoName, promoWorth.toInt(),couponLife.toInt(),couponPrice.toFloat())
                database.child("biz_owners").child(biz_uid)
                    .child(global_location_key.toString()).child("coupons").child(promoKey).setValue(coupon_insert)

                //dismiss dialog
                mAlertDialog.dismiss()
                // restart activity
//                            finish();
//                            startActivity(getIntent());

            } else {

                Toast.makeText(
                    baseContext, "ใส่ข้อมูลไม่ครบครับ",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

        //cancel button click of custom layout
        mDialogView.dialogCancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends


    }

   // coupon write new/del/edit functions

    private fun write_new_coupon(){

        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_coupon_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("เพิ่ม coupon card")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)


        // check null




        // button click of custom layout
        mDialogView.dialogAddBtn.setOnClickListener {


            var promoName = mDialogView.new_promo_name.text.toString()
            var promoWorth = mDialogView.promo_worth.text.toString()
            var couponLife = mDialogView.coupon_life.text.toString()
            var couponPrice = mDialogView.coupon_price.text.toString()
//            var couponTerm = mDialogView.coupon_term.text.toString()

            if (promoName.isNotEmpty() && promoWorth.isNotEmpty()&& couponLife.isNotEmpty()&&couponPrice.isNotEmpty()) {

                var biz_uid = auth.currentUser!!.uid



                var coupon_insert = Coupons(promoName, promoWorth.toInt(),couponLife.toInt(),couponPrice.toFloat())
                database.child("biz_owners").child(biz_uid)
                    .child(global_location_key.toString()).child("coupons").push()
                    .setValue(coupon_insert)

                //dismiss dialog
                mAlertDialog.dismiss()
                // restart activity
//                       finish();
//                       startActivity(getIntent());

            } else {

                Toast.makeText(
                    baseContext, "ใส่ข้อมูลไม่ครบครับ",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

        //cancel button click of custom layout
        mDialogView.dialogCancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }
    // write new coupon ends

    // coupon delete
    private fun coupon_delete(promoKey:String,promoName:String){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.promo_del_confirmation_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.promo_del_name.text = promoName
        //login button click of custom layout
        mDialogView.del_conf_DelBtn.setOnClickListener {

            // delete from database

            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("coupons").child(promoKey).removeValue()


            //dismiss dialog
            mAlertDialog.dismiss()

            //restart activity
//            finish();
//            startActivity(getIntent());

        }
        //cancel button click of custom layout
        mDialogView.del_conf_CancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }

    // ends

    // coupon edit

    private fun email_edit(){

        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.reauth,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)




        // enable btn if text field is not empty - start

        mDialogView.reauth_next.setEnabled(false)


        mDialogView.reauth_pw.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString().trim { it <= ' ' }.length > 5) {
                    mDialogView.reauth_next.setEnabled(true)
                    mDialogView.reauth_next.setBackgroundColor(resources.getColor(R.color.colorBackground))
                } else {
                    mDialogView.reauth_next.setEnabled(false)
                    mDialogView.reauth_next.setBackgroundColor(resources.getColor(R.color.colorDisabled))
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










        //button click of custom layout
        mDialogView.reauth_next.setOnClickListener {

            // show  progress bar

            mDialogView.progressBarHorizontal.visibility = View.VISIBLE

            val pw_input = mDialogView.reauth_pw.text.toString()

            // sign user in
            val currentEmail = auth.currentUser!!.email
            auth.signInWithEmailAndPassword(currentEmail.toString(), pw_input)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        // hide  progress bar

                        mDialogView.progressBarHorizontal.visibility = View.INVISIBLE


                        //dismiss dialog
                        mAlertDialog.dismiss()

                        // inflate new email dialog


                        //Inflate the new email/pw dialog
                        val mDialogView =
                            LayoutInflater.from(this@dashboard).inflate(R.layout.email_update, null)

                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(this@dashboard)
                            .setView(mDialogView)
                            .setTitle("")
                        //show dialog
                        val mAlertDialog = mBuilder.show()

                        // disable touch outside

                        mAlertDialog.setCanceledOnTouchOutside(false)
                        //login button click of custom layout
                        mDialogView.newemail_save.setOnClickListener {

                            mDialogView.dialog_progressbar.visibility = View.VISIBLE


                            val newemail_input = mDialogView.dialog_newemail.text.toString()
                            val newpw_input = mDialogView.dialog_newpw.text.toString()

                            auth.currentUser!!.updateEmail(newemail_input)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                    // update email success

                                        auth.currentUser!!.updatePassword(newpw_input)
                                            .addOnCompleteListener { task ->
                                                if(task.isSuccessful){
                                                    // update pw success

                                                    toast("password updated")
                                                    //dismiss dialog
                                                    mAlertDialog.dismiss()
                                                    finish();
                                                    startActivity(getIntent());
                                                }else{
                                                    // update pw failed
                                                    toast(task.exception.toString())
                                                }
                                            }





                                    }else{
                                        // update email failed
                                        toast(task.exception.toString())
                                    }
                                }
                                }

                        mDialogView.newemail_cancel.setOnClickListener {

                            // dismiss dialog on cancel

                            mAlertDialog.dismiss()
                        }




                        }else{
                        // reauth failed

                        mAlertDialog.dismiss()

                        // Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(this@dashboard)
                            .setView(mDialogView)
                            .setTitle("ตรวจสอบ password ไม่ผ่าน")
                        //show dialog
                        val  mAlertDialog = mBuilder.show()
                        mDialogView.confirm_title.text = "error code:"
                        mDialogView.confirm_body.text = task.exception?.message
                        mDialogView.confirm_next.text = "ย้อนกลับ"
                        mDialogView.confirm_cancel.visibility = View.GONE
                        //login button click of custom layout
                        mDialogView.confirm_next.setOnClickListener {
                            //dismiss dialog
                            mAlertDialog.dismiss()

                            }
                        //cancel button click of custom layout
                        mDialogView.confirm_cancel.setOnClickListener {
                            //dismiss dialog
                            mAlertDialog.dismiss()
                        }

                        // dialog with editText ends

                    }


//                        //dismiss dialog
//                        mAlertDialog.dismiss()

                    }
                }
        //cancel button click of custom layout
        mDialogView.reauth_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }

fun toast(msg:String){

        Toast.makeText(
            baseContext, msg,
            Toast.LENGTH_LONG
        ).show()


    }

    private fun add_new_device(){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()
        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.confirm_title.text = "พบเคร่ื่องใหม่"
        mDialogView.confirm_body.text = "เพ่ิมเครื่องนี้ใน account ของคุณ?"
        mDialogView.confirm_cancel.text ="ออกจากระบบ"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {

            device_location_select()






            //dismiss dialog
            mAlertDialog.dismiss()


        }
        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            // sign out
            auth.signOut()
            startActivity(Intent(this,launcher_land::class.java))

        }

        // dialog with editText ends

    }

    private fun device_location_select(){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
            .setCancelable(false)
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.frame_title.text = "เลือกสาขาสำหรับเครื่องนี้"

        // read location database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var loc_select_ref  = database.child("biz_owners").child(biz_uid).child("locations")

        val loc_select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var radioID = 0
                for (ds in dataSnapshot.children) {

                    var locName = ds.child("locationName").getValue(String::class.java)
                    var locKey = ds.key

                    val radioButton = RadioButton(this@dashboard)
                    radioID += 1

                    radioButton.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton.setText(locName)
                    radioButton.id = radioID
                    radioButton.tag = locKey
                    radioButton.isChecked = true



                    mDialogView.radioSet_group.addView(radioButton)





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

        loc_select_ref.addValueEventListener(loc_select_listener)
        // read location database ends


        //next btn
        mDialogView.frame_next.setOnClickListener {

            // get id from radio group

            var id: Int = mDialogView.radioSet_group.checkedRadioButtonId


            if(id!=-1){
                val radio:RadioButton =mDialogView.radioSet_group.findViewById(id)
                global_location_key = radio.tag.toString()

                // add device to database

            var currentDeviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            var biz_uid = auth.currentUser!!.uid

            // if not set new device info
            var device_info = Devices(currentDeviceID,currentDeviceID, global_location_key)

            database.child("biz_owners").child(biz_uid).child("devices").push().setValue(device_info)

                // set global var

                global_device_id = currentDeviceID

                //dismiss dialog
                mAlertDialog.dismiss()

            }else{

                // no radio selected

                toast("กดเลือกสาขาด้วยครับ")
            }









        }


        // dialog with editText ends


    }

    private fun location_edit(){



        // dialog with edittext start
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_device_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("ตั้งชื่อสาขานี้")


        mDialogView.icon.setImageResource(R.drawable.location_icon)
        mDialogView.dialog_newdevice.setHint("ใส่ชื่อสาขาใหม่")

        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)


        // enable btn if text field is not empty - start

        mDialogView.dialog_newdevice.addTextChangedListener(object : TextWatcher {
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

            var biz_uid = auth.currentUser!!.uid

            var new_loc_name = mDialogView.dialog_newdevice.text.toString()

            database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString()).child("locationName").setValue(new_loc_name)




            //dismiss dialog
            mAlertDialog.dismiss()


        }
        //cancel button click of custom layout
        mDialogView.newdevice_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }


    private fun location_select(){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
            .setCancelable(false)
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.frame_title.text = "เลือกสาขาสำหรับเครื่องนี้"

        // read location database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var loc_select_ref  = database.child("biz_owners").child(biz_uid).child("locations")

        val loc_select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var radioID = 0
                for (ds in dataSnapshot.children) {

                    var locName = ds.child("locationName").getValue(String::class.java)
                    var locKey = ds.key

                    val radioButton = RadioButton(this@dashboard)
                    radioID += 1

                    radioButton.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton.setText(locName)
                    radioButton.id = radioID
                    radioButton.tag = locKey

                    if(locKey == global_location_key){
                        radioButton.isChecked = true
                    }



                    mDialogView.radioSet_group.addView(radioButton)





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

        loc_select_ref.addValueEventListener(loc_select_listener)
        // read location database ends


        //next btn
        mDialogView.frame_next.setOnClickListener {

            // get id from radio group

            var id: Int = mDialogView.radioSet_group.checkedRadioButtonId


            if(id!=-1){
                //set location key
                val radio:RadioButton =mDialogView.radioSet_group.findViewById(id)
                global_location_key = radio.tag.toString()

                // change location of this device

                database.child("biz_owners").child(biz_uid).child("devices").child(global_device_key.toString()).child("locationKey").setValue(radio.tag.toString())

                //dismiss dialog
                mAlertDialog.dismiss()

//                //restart activity
//                finish()
//                startActivity(getIntent())

            }else{

                // no radio selected

                toast("กดเลือกสาขาด้วยครับ")
            }









        }

        //cancel button click of custom layout
        mDialogView.frame_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }


        // dialog with editText ends


    }


    private fun location_del(){


        // read location database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var loc_select_ref  = database.child("biz_owners").child(biz_uid).child("locations")

        val loc_select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var radioID = 0




                if(dataSnapshot.childrenCount < 2){



                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@dashboard)
                        .setView(mDialogView)
                        .setTitle("ลบ location ไม่สำเร็จ")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()

                    // disable touch outside

                    mAlertDialog.setCanceledOnTouchOutside(false)


                    mDialogView.confirm_title.visibility = View.GONE
                    mDialogView.confirm_body.text = "ระบบต้องการอย่างน้อย 1 location"
                    mDialogView.confirm_next.text = "ย้อนกลับ"
                    //login button click of custom layout
                    mDialogView.confirm_next.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()

                    }
                    //cancel button click of custom layout
                    mDialogView.confirm_cancel.visibility = View.GONE

                    // dialog with editText ends

                    return



                }
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@dashboard)
                    .setView(mDialogView)
                    .setTitle("")
                    .setCancelable(false)
                //show dialog
                val  mAlertDialog = mBuilder.show()

                // disable touch outside

                mAlertDialog.setCanceledOnTouchOutside(false)

                mDialogView.frame_title.text = "เลือกสาขาที่ต้องการลบ"

                for (ds in dataSnapshot.children) {

                    var locName = ds.child("locationName").getValue(String::class.java)
                    var locKey = ds.key

                    val radioButton = RadioButton(this@dashboard)
                    radioID += 1

                    radioButton.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton.setText(locName)
                    radioButton.id = radioID
                    radioButton.tag = locKey

                    if(locKey == global_location_key){
                        radioButton.isChecked = true
                    }




                    mDialogView.radioSet_group.addView(radioButton)


                }

                //next btn
                mDialogView.frame_next.setOnClickListener {

                    // get id from radio group

                    var id: Int = mDialogView.radioSet_group.checkedRadioButtonId



                    if(id!=-1){
                        //set location key
                        val radio:RadioButton =mDialogView.radioSet_group.findViewById(id)

                        //dismiss dialog
                        mAlertDialog.dismiss()

                        // call confirm delete fun

                        loc_del_conf(radio.text.toString(),radio.tag.toString())

                    }else{

                        // no radio selected

                        toast("กดเลือกสาขาด้วยครับ")
                    }

                }

                //cancel button click of custom layout
                mDialogView.frame_cancel.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }


                // dialog with editText ends


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

        loc_select_ref.addListenerForSingleValueEvent(loc_select_listener)
        // read location database ends





    }

    private fun loc_del_conf(locName:String,lockey:String){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.confirm_body.text = "ลบ "+locName+" และข้อมูลของสาขานี้ออกจาก account ของคุณ"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child(lockey).removeValue()
            database.child("biz_owners").child(biz_uid).child("locations").child(lockey).removeValue()






            //dismiss dialog
            mAlertDialog.dismiss()

            location_select()


        }
        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends


    }

    private fun device_del(){



        // read location database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var dev_select_ref  = database.child("biz_owners").child(biz_uid).child("devices")

        val dev_select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {


                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@dashboard)
                    .setView(mDialogView)
                    .setTitle("")
                    .setCancelable(false)
                //show dialog
                val  mAlertDialog = mBuilder.show()

                // disable touch outside

                mAlertDialog.setCanceledOnTouchOutside(false)


                mDialogView.frame_title.text = "เลือกเครื่องที่ต้องการลบ"


                var radioID = 0
                for (ds in dataSnapshot.children) {

                    var devName = ds.child("deviceName").getValue(String::class.java)
                    var devID = ds.child("deviceID").getValue(String::class.java)
                    var devKey = ds.key

                    val radioButton = RadioButton(this@dashboard)
                    radioID += 1

                    radioButton.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton.setText(devName)
                    radioButton.id = radioID
                    radioButton.tag = devKey



                    // check button if this device
                    if(devID == global_device_id){
                        radioButton.isChecked = true
                    }




                    mDialogView.radioSet_group.addView(radioButton)


                }
                //next btn
                mDialogView.frame_next.setOnClickListener {

                    // get id from radio group

                    var id: Int = mDialogView.radioSet_group.checkedRadioButtonId


                    if(id!=-1){
                        //set location key
                        val radio:RadioButton =mDialogView.radioSet_group.findViewById(id)

                        //dismiss dialog
                        mAlertDialog.dismiss()

                        // call confirm delete fun

                        self_del(radio.text.toString(),radio.tag.toString())

                    }else{

                        // no radio selected

                        toast("กดเลือก device ด้วยครับ")
                    }









                }

                //cancel button click of custom layout
                mDialogView.frame_cancel.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }


                // dialog with editText ends


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

        dev_select_ref.addListenerForSingleValueEvent(dev_select_listener)
        // read location database ends





    }


    private fun self_del(devName:String,devKey:String){



        var biz_uid = auth.currentUser!!.uid

        var self_del_ref  = database.child("biz_owners").child(biz_uid).child("devices").child(devKey)

        val self_del_listener = object : ValueEventListener {

            var devID = ""

            override fun onDataChange(dataSnapshot: DataSnapshot) {



                for (ds in dataSnapshot.children) {


                    if (ds.key == "deviceID"){


                        devID = ds.getValue(String::class.java)!!


                    }



                }

                // if self delete

                if(devID == global_device_id){

                    self_del_conf(devKey)

                }else{

                    // call confirm delete fun

                    dev_del_conf(devName,devKey)


                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        self_del_ref.addListenerForSingleValueEvent(self_del_listener)
        // read location database ends



    }


    private fun self_del_conf(devKey:String){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.confirm_body.text = "ลบเครื่องนี้ออกจาก account ของคุณ "
        //login button click of custom layout

        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

        mDialogView.confirm_next.setOnClickListener {


            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child("devices").child(devKey).removeValue()

            // reset global device id

            global_device_id = null

            // dismiss

            mAlertDialog.dismiss()







        }



    }


    private fun dev_del_conf(devName:String,devkey:String){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.confirm_body.text = "ลบ "+devName+" ออกจาก account ของคุณ"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child("devices").child(devkey).removeValue()






            //restart activity
            mAlertDialog.dismiss()




        }
        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends


    }



    private fun transaction_database_read(){

        var transaction_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("type").equalTo("sale")

        val transaction_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //reset counter
                sales_sum = 0f
                upsales_sum = 0f
                for (ds in dataSnapshot.children) {

//                    heart_sum += ds.child("heartBank").getValue(Int::class.java)!!
//                    heart_monthly.text = heart_sum.toString()
                    sales_sum += ds.child("amount").getValue(Float::class.java)!!
                    sales_monthly.text = sales_sum.toString()
                    // set global var
                    global_sales = sales_sum.toInt()
                    upsales_sum += ds.child("upsale").getValue(Float::class.java)!!
                    upsales_monthly.text = upsales_sum.toString()
                    // global var
                    global_upsales = upsales_sum.toInt()


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

        total_customer_read()



    }

    private fun total_customer_read() {
        var total_customers_ref =
            database.child("customers").orderByChild("loc_key").equalTo(global_location_key)

        val total_customers_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //reset counter
                total_customer_sum = 0
                for (ds in dataSnapshot.children)

                    total_customer_sum += 1
                total_customers.text = total_customer_sum.toString()
                global_customers = total_customer_sum


            }




        override fun onCancelled(databaseError: DatabaseError) {

            // Getting Post failed, log a message
            Log.w("error", databaseError.toException())
            // [START_EXCLUDE]
            Toast.makeText(
                baseContext, "database failed",
                Toast.LENGTH_SHORT
            ).show()
            // [END_EXCLUDE]
        }
    }


        total_customers_ref.addListenerForSingleValueEvent(total_customers_listener)
        // database ends

        user_database_read()



    }



    // payment filters


    private fun user_database_read(){


        // get device data from database

        var biz_uid = auth.currentUser!!.uid

        var user_ref  = database.child("biz_owners").child(biz_uid)

        val user_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {




                for (ds_treshold in dataSnapshot.child("tresholds").children) {


                    when(ds_treshold.key){
                        "customers" ->  customers_treshold = ds_treshold.getValue(Int::class.java)!!
                        "sales" -> sales_treshold = ds_treshold.getValue(Float::class.java)!!
                        "upsales" -> upsales_treshold = ds_treshold.getValue(Float::class.java)!!
                    }


                }



                // check account expiration date



                for (ds_status in dataSnapshot.child("info").children) {

                    if (ds_status.key  == "date_exp"){

                        if (ds_status.getValue(Long::class.java) !== null){
                            db_exp_time = ds_status.getValue(Long::class.java)!!
                            global_exp_date = db_exp_time
                        }

                    }

                    if (ds_status.key  == "requested_time_limit"){

                        if (ds_status.getValue(Long::class.java) !== null){
                            db_requested_time_limit = ds_status.getValue(Long::class.java)!!

                        }

                    }

                // check status
                    if (ds_status.key == "status"){


                        when(ds_status.getValue(Int::class.java)){
                            // status: disabled
                            0 ->  {

                                val intent = Intent(this@dashboard,congrats::class.java)
                                intent.putExtra("lock", true)


                                startActivity(intent)


                            }

                            // status: trial
                            1 ->  if (total_customer_sum > customers_treshold ||
                                sales_sum > sales_treshold ||
                                upsales_sum > upsales_treshold){

                                //if trial ends set due date then change status
                                val time = System.currentTimeMillis() / 1000L
                                val exp_time = time+(86400*5)

//                                val exp_date = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
//                                    .format(java.time.Instant.ofEpochSecond(exp_time))


                                database.child("biz_owners").child(biz_uid).child("info").child("date_exp").setValue(exp_time)

                                database.child("biz_owners").child(biz_uid).child("info").child("status").setValue(2)
                                //set global status
                                global_status = 2

                                // send to congrats
                                val intent = Intent(this@dashboard,congrats::class.java)
                                intent.putExtra("sales", sales_sum)
                                intent.putExtra("upsales", upsales_sum)
                                intent.putExtra("customers", total_customer_sum)

                                startActivity(intent)

                            }
                            // status: pending payment - grace period = display reminder & detect expiration date
                            2 -> {

                                // check exp date
                                if (db_exp_time < System.currentTimeMillis() / 1000L){

                                        database.child("biz_owners").child(biz_uid).child("info").child("status").setValue(0)
                                        //set global status
                                        global_status = 0

                                    // send to congrats

                                    val intent = Intent(this@dashboard,congrats::class.java)
                                    intent.putExtra("sales", sales_sum)
                                    intent.putExtra("upsales", upsales_sum)
                                    intent.putExtra("customers", total_customer_sum)

                                    startActivity(intent)


                                    }

                                // display warning

                                    reminder.visibility = View.VISIBLE
//                                    val exp_date = java.time.format.DateTimeFormatter.ofPattern("dd/MM/YY").withZone(ZoneId.systemDefault())
//                                    .format(java.time.Instant.ofEpochSecond(db_exp_time))

                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
                                val date = java.util.Date(db_exp_time * 1000)
                                val exp_date = sdf.format(date)

                                    exp_display.text = exp_date

                                    paynow.visibility = View.VISIBLE


                                }

                            // paid - showing expiration date and paynow button
                            3 ->  {

                                // check exp date
                                if (db_exp_time < System.currentTimeMillis() / 1000L){

                                    //if trial ends set due date then change status
                                    val time = System.currentTimeMillis() / 1000L
                                    val exp_time = time+(86400*5)

//                                val exp_date = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
//                                    .format(java.time.Instant.ofEpochSecond(exp_time))


                                    database.child("biz_owners").child(biz_uid).child("info").child("date_exp").setValue(exp_time)

                                    database.child("biz_owners").child(biz_uid).child("info").child("status").setValue(2)
                                    //set global status
                                    global_status = 2
                                }



                                exp.visibility = View.VISIBLE
                                paynow2.visibility = View.VISIBLE
//                                val exp_date = java.time.format.DateTimeFormatter.ofPattern("dd/MM/YY").withZone(ZoneId.systemDefault())
//                                .format(java.time.Instant.ofEpochSecond(db_exp_time))


                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
                                val date = java.util.Date(db_exp_time * 1000)
                                val exp_date = sdf.format(date)

                                exp_date_display.text = exp_date







                            }
                            // pending payment confirmation
                            4 -> {

                                // check exp date and confirm time
                                if (db_requested_time_limit < System.currentTimeMillis() / 1000L && db_exp_time < System.currentTimeMillis() / 1000L ){

                                    //Inflate the dialog with custom view
                                    val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

                                    //AlertDialogBuilder
                                    val mBuilder = AlertDialog.Builder(this@dashboard)
                                        .setView(mDialogView)
                                        .setTitle("")
                                        .setCancelable(false)
                                    //show dialog
                                    val  mAlertDialog = mBuilder.show()

                                    // disable touch outside

                                    mAlertDialog.setCanceledOnTouchOutside(false)

                                    mDialogView.frame_title.text = "กรุณาติดต่อ LINE@Pratupjai เพื่อยืนยันการชำระเงิน"

                                    //next btn
                                    mDialogView.frame_next.visibility = View.GONE

                                    // cancel btn
                                    mDialogView.frame_cancel.visibility = View.GONE



                                }


                                // display warning
                                textView30.text = "รอยืนยันการโอนเงินกับประทับใจ"
                                reminder.visibility = View.VISIBLE

                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
                                val date = java.util.Date(db_exp_time * 1000)
                                val exp_date = sdf.format(date)

                                exp_date_display.text = exp_date
                                exp.visibility = View.VISIBLE

                            }

                        }
                    }




                }




            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("error",  databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Failed to load.",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }


        user_ref.addValueEventListener(user_listener)

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
