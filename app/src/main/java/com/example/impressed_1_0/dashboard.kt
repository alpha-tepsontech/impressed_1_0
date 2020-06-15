package com.example.impressed_1_0

// set up sensor events

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.Image
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.heart_worth_dialog.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.*
import kotlinx.android.synthetic.main.new_loc_dialog.view.*


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

//    var loc_key = global_location_key.toString()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference
        // init ends

        // call device_read to get all the elements from the database
        device_database_read()




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
        location_edit.setOnClickListener {
            new_loc_dialog()
        }

        device_edit.setOnClickListener {
            new_device_dialog()
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


                for (ds in dataSnapshot.children) {
                    val location_key = ds.child("locationKey").getValue()
                    global_location_key = location_key.toString()
                    val deviceName = ds.child("deviceName").getValue()
                    device_display.text = deviceName.toString()
                }

                // call location_read after key is obtained
                location_database_read()
                promos_database_read()
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


        device_ref.addListenerForSingleValueEvent(device_listener)

        Log.d("test_after_device_fun", global_location_key)
        // database ends

    }

    private fun location_database_read(){


        var biz_uid = auth.currentUser!!.uid

        var location_ref  = database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString())

        val location_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                location_display.text = dataSnapshot.child("locationName").getValue().toString()
                heart_display.text = dataSnapshot.child("heartWorth").getValue().toString()

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
        //login button click of custom layout
        mDialogView.newloc_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var new_loc = mDialogView.dialog_newlocation.text.toString()

            database.child("biz_owners").child(biz_uid).child("locations").child(new_loc).setValue(1)

            //dismiss dialog
            mAlertDialog.dismiss()

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
        //login button click of custom layout
        mDialogView.newdevice_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var new_device = mDialogView.dialog_newdevice.text.toString()

            database.child("biz_owners").child(biz_uid).child("devices").child(new_device).setValue(1)

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

        Log.d("test",biz_uid.toString())

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("promos").orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)
                    Log.d("test",promoName)
                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)

//                    val promo_linear_layout = LinearLayout(this@dashboard)
//
//                    promo_linear_layout.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
//                    promo_linear_layout.orientation = LinearLayout.HORIZONTAL
//
//                    val promoName_textview = TextView(this@dashboard)
//                    promoName_textview.text = promoName
//
//
//                    var promoName_textview_params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
//                    promoName_textview_params.setMargins(0,0,10,0)
//
////                    (promoName_textview.layoutParams as LinearLayout.LayoutParams).weight = 0.6.toFloat()
//                    val param = promoName_textview.layoutParams as LinearLayout.LayoutParams
//                    param.weight = 0.5f
//                    promoName_textview.layoutParams = param
//
////                    promoName_textview.layoutParams = promoName_textview_params
//
//                    promoName_textview.setPadding(10,0,10,0)
//                    promoName_textview.setBackgroundResource(R.drawable.edittext_border)
//                    val sukhumvit_bold : Typeface? = ResourcesCompat.getFont(this@dashboard, R.font.sukhumvit_bold)
//
//
//                    promoName_textview.typeface = sukhumvit_bold
//
//
//                    val btn_frame = FrameLayout(this@dashboard)
//                    var btn_frame_params =  ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, 50)
////                    btn_frame_params.horizontalWeight = 1.0f
//                    btn_frame.layoutParams = btn_frame_params
//
//
//                    val heart_btn = ImageButton(this@dashboard)
//                    heart_btn.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
//                    heart_btn.setImageResource(R.drawable.heart_light)
////                    heart_btn.setBackgroundResource(null)
//                    heart_btn.scaleType = ImageView.ScaleType.FIT_START
//
//                    val heart_text = TextView(this@dashboard)
//                    var heart_text_params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
////                    heart_text_params.horizontalWeight = 1.0f
//                    heart_text.gravity = Gravity.END
//                    heart_text.text = promoWorth.toString()
//
//
//                    btn_frame.addView(heart_btn)
//                    btn_frame.addView(heart_text)
//
//
////                    var heart = "\u2764"
////
////                    heart_btn.text = heart+promoWorth.toString()
//                    val edit_btn = ImageButton(this@dashboard)
//
//                    var edit_btn_params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,50)
////                    edit_btn_params.horizontalWeight = 1.0f
//
//                    edit_btn.layoutParams = edit_btn_params
//                        edit_btn.setImageResource(R.drawable.edit_light)
//                    edit_btn.scaleType = ImageView.ScaleType.FIT_CENTER
//                    val delete_btn = ImageButton(this@dashboard)
//                    delete_btn.layoutParams = ConstraintLayout.LayoutParams(50,50)
//                    delete_btn.setImageResource(R.drawable.heart)
//                    delete_btn.scaleType = ImageView.ScaleType.FIT_CENTER
//
//


//                    promo_frame.addView(promo_linear_layout)
//                    promo_linear_layout.addView(promoName_textview)
//                    promo_linear_layout.addView(btn_frame)
//                    promo_linear_layout.addView(edit_btn)
//                    promo_linear_layout.addView(delete_btn)

                    val btn_set = LayoutInflater.from(this@dashboard).inflate(R.layout.promo_btn_set,null)
                    val promoName_holder = btn_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = btn_set.findViewById<TextView>(R.id.promoWorth_textview)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()
                    promo_frame.addView(btn_set)

                    val edit_btn = btn_set.findViewById<ImageButton>(R.id.edit_btn)

                    edit_btn.setOnClickListener{
                        //TODO implement edit btn
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

        promos_ref.addListenerForSingleValueEvent(promos_listener)
        // database ends




    }




}
