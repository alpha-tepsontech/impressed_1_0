package com.example.impressed_1_0

// set up sensor events

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_device_id
import com.example.impressed_1_0.MyApplication.Companion.global_device_key
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.example.impressed_1_0.MyApplication.Companion.global_status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_biz_auth.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.dialog_frame.view.*
import kotlinx.android.synthetic.main.email_update.view.*
import kotlinx.android.synthetic.main.heart_worth_dialog.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.*
import kotlinx.android.synthetic.main.new_loc_dialog.view.*
import kotlinx.android.synthetic.main.new_promo_dialog.view.*
import kotlinx.android.synthetic.main.new_promo_dialog.view.dialogCancelBtn
import kotlinx.android.synthetic.main.promo_del_confirmation_dialog.view.*
import kotlinx.android.synthetic.main.radio_set.*
import kotlinx.android.synthetic.main.reauth.view.*
import java.util.*

// set elements vars
private var heart_sum = 0
private var sales_sum:Float = 0f
private var upsales_sum:Float = 0f
private var total_customer_sum:Int = 0

// set treshold vars

private var sales_treshold:Float = 0f
private var upsales_treshold:Float = 0f
private var customers_treshold:Int = 0


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
            auth.signOut()
            startActivity(Intent(this,launcher_land::class.java))
        }

        heart_edit.setOnClickListener {

            heart_dialog()
        }
        location_edit.setOnClickListener {
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

            write_new_promo()

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

                if (global_device_id != deviceID){

                    add_new_device()

                }

                // call location_read after key is obtained

                location_database_read()
                promos_database_read()
                transaction_database_read()
                total_customer_read()
                user_database_read()
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
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.heart_save.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid

            var heart_worth = mDialogView.dialog_heart_value.text.toString()

            database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString()).child("heartWorth").setValue(heart_worth)

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

            var new_heartWorth = mDialogView.dialog_heart_worth.text.toString().toInt()

            var location_key = database.push().key.toString()

            var location_info = Location(new_loc,new_heartWorth)

            database.child("biz_owners").child(biz_uid).child("locations").child(location_key).setValue(location_info)

            global_location_key = location_key


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

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("promos").orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // clean out frame
                promo_frame.removeAllViews()


                for (ds in dataSnapshot.children) {

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

        private fun write_new_promo(){

            // dialog with edittext start
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.new_promo_dialog,null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this@dashboard)
                .setView(mDialogView)
                .setTitle("")
            //show dialog
            val  mAlertDialog = mBuilder.show()

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
                        .setTitle("")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()

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


        // enable btn if text field is not empty - start

        mDialogView.reauth_next.setEnabled(false)


        mDialogView.reauth_pw.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString().trim { it <= ' ' }.length == 0) {
                    mDialogView.reauth_next.setEnabled(false)
                } else {
                    mDialogView.reauth_next.setEnabled(true)
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

            val pw_input = mDialogView.reauth_pw.text.toString()

            // sign user in
            val currentEmail = auth.currentUser!!.email
            auth.signInWithEmailAndPassword(currentEmail.toString(), pw_input)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
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
                        //login button click of custom layout
                        mDialogView.newemail_save.setOnClickListener {

                            mDialogView.dialog_progressbar.visibility = View.VISIBLE

                            mDialogView.newemail_save.setEnabled(false)
                            mDialogView.newemail_cancel.setEnabled(false)

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




                        }else{

                        // sign in failed
                        toast(task.exception.toString())
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

        mDialogView.confirm_title.text = "พบเคร่ื่องใหม่"
        mDialogView.confirm_body.text = "เพ่ิมเครื่องนี้ใน account ของคุณ?"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {

            device_location_select()






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

                //dismiss dialog
                mAlertDialog.dismiss()

            }else{

                // no radio selected

                toast("กดเลือกสาขาด้วยครับ")
            }









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


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.dialog_frame,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
            .setCancelable(false)
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.frame_title.text = "เลือกร้านที่ต้องการลบ"

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

    private fun loc_del_conf(locName:String,lockey:String){

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@dashboard).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.confirm_body.text = "ลบ "+locName+" และข้อมูลของสาขานี้ออกจาก account ของคุณ"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {

            var biz_uid = auth.currentUser!!.uid
            database.child("biz_owners").child(biz_uid).child(lockey).removeValue()
            database.child("biz_owners").child(biz_uid).child("locations").child(lockey).removeValue()






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

   private fun device_del(){

//TODO: implement device managment
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
                    upsales_sum += ds.child("upsale").getValue(Float::class.java)!!
                    upsales_monthly.text = upsales_sum.toString()


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

                if (dataSnapshot.child("info").child("date_exp").exists()) {

                    for (ds_expiration in dataSnapshot.child("info").child("date_exp").children) {


                        when (ds_expiration.key) {
                            "time" -> Log.d(
                                "test-exp",
                                ds_expiration.getValue(Int::class.java).toString()
                            )

                        }


                    }
                }
                //end if



                // check status

                for (ds_status in dataSnapshot.child("info").children) {

                    if (ds_status.key == "status"){


                        when(ds_status.getValue(Int::class.java)){
                            // status: disabled
                            0 ->  startActivity(Intent(this@dashboard,congrats::class.java))
                            // status: trial
                            1 ->  if (total_customer_sum > customers_treshold ||
                                sales_sum > sales_treshold ||
                                upsales_sum > upsales_treshold){

                                //if trial ends set due date then change status

                                var due_date: Date = Date()

                                database.child("biz_owners").child(biz_uid).child("info").child("date_exp").setValue(due_date)

                                database.child("biz_owners").child(biz_uid).child("info").child("status").setValue(2)
                                //set global status
                                global_status = 2
                                startActivity(Intent(this@dashboard,congrats::class.java))

                            }
                            // status: pending payment - grace period = display reminder
//                            2 ->
                            // status: paid
//                            3 -> // check paid date and current date if over send to payment choice
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


}
