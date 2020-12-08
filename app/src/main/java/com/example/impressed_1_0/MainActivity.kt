package com.example.impressed_1_0

//import kotlinx.android.synthetic.main.activity_phone_authentication.*


// import elements from verification_dialog view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.util.concurrent.TimeUnit

// set up sensor events
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.Sensor
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_name
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_location_key

// import firebase database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.name_dialog.view.*
import java.util.*

// twillio sms
//
//import com.twilio.Twilio
//import com.twilio.rest.api.v2010.account.Message
//import com.twilio.type.PhoneNumber








class MainActivity : AppCompatActivity() , SensorEventListener{

    // init sensor vars
    private var mSensorManager : SensorManager ?= null
    private var mAccelerometer : Sensor ?= null

    // init sensor vars ends


    // firebase auth setup

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""
    var verification_code = ""
    var name_input = ""

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    private var customer_phone_listener: ValueEventListener? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // jumper

        startActivity(Intent(this,payment::class.java))

        // ends

        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // gravity sensor setup ends

        // disable the btn until input is entered
        log_in_btn.setEnabled(false)

        welcome_text()


        // custom keyboard start
        // disable softkeyboard on focus

        phnNoTxt.setShowSoftInputOnFocus(false);

        // diasble softkeyboard on focus ends


        letter9.setOnClickListener {

            phnNoTxt.append("9")

        }
        letter8.setOnClickListener {

            phnNoTxt.append("8")

        }
        letter7.setOnClickListener {

            phnNoTxt.append("7")

        }
        letter6.setOnClickListener {

            phnNoTxt.append("6")

        }
        letter5.setOnClickListener {

            phnNoTxt.append("5")

        }

        letter4.setOnClickListener {

            phnNoTxt.append("4")

        }
        letter3.setOnClickListener {

            phnNoTxt.append("3")

        }
        letter2.setOnClickListener {

            phnNoTxt.append("2")

        }
        letter1.setOnClickListener {

            phnNoTxt.append("1")

        }
        letter0.setOnClickListener {

            phnNoTxt.append("0")

        }

        del.setOnClickListener {
            //delete last digit
            var droped = phnNoTxt.text.dropLast(1)
            //set text
            phnNoTxt.setText(droped)
            //put cursor to the end
            phnNoTxt.setSelection(phnNoTxt.text.length);


            // clear edit text
//            phnNoTxt.setText("")



        }


        // custom keyboard ends


        // firebase auth
        mAuth = FirebaseAuth.getInstance()

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends

        // enable btn if text field is not empty - start

        phnNoTxt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if  (start == 2 && before ==0){

                    phnNoTxt.append("-")

                }
                else if  (start == 6 && before ==0){

                    phnNoTxt.append("-")

                }else if (s.toString().trim { it <= ' ' }.length == 12){
                    log_in_btn.setEnabled(true)

                }else{
                    log_in_btn.setEnabled(false)

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


        log_in_btn.setOnClickListener {
                view: View? -> progress.visibility = View.VISIBLE
//                Log.d("test","btn click")
            if(phnNoTxt.text.isNotEmpty()){

                checkifexist()
            }else{
//                    view: View? -> progress.visibility = View.INVISIBLE
// dialog box code start
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(this)

                // set message of alert dialog
                dialogBuilder.setMessage("ใส่เบอร์โทรศัพท์ได้เลยครับ")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                        dialog, id ->dialog.cancel()
                    })

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("")
                // show alert dialog
                alert.show()

// dialog box code ends



            }
        }

        logo.setOnLongClickListener {
            startActivity(Intent(this,biz_dashboard::class.java))
            true
        }
    }


    private fun checkifexist(){

        val country_code = "+66"
        val phnNoClean = phnNoTxt.text.toString().replace("-","")
        val phnNo = country_code+phnNoClean


        // get data from database
        var customersref  = database.child("customers").orderByChild("phone").equalTo(phnNo)

        val customer_phone_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var phonecheck = dataSnapshot.getValue()

                if (phonecheck == null){
                    verify()
                }
                else if(phonecheck !== null) {

                    for (ds in dataSnapshot.children) {
                        val phonerecorded = ds.child("phone").getValue(String::class.java)
                        val namerecorded = ds.child("name").getValue(String::class.java)

                        if (phonerecorded == phnNo) {
                            customer_logged_phone = phonerecorded
                            customer_logged_name = namerecorded
                            startActivity(Intent(this@MainActivity, customer::class.java))

                        }

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
        customersref.addListenerForSingleValueEvent(customer_phone_listener)

        // database ends

    }






    private fun verify () {

        verificationCallbacks()
        val country_code = "+66"
        val phnNoClean = phnNoTxt.text.toString().replace("-","")
        val phnNo = country_code+phnNoClean

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phnNo,
            120,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )
    }

    private fun verificationCallbacks () {
        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Log.d("test","Vfailed")
                progress.visibility = View.INVISIBLE
            }


            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {
                // dialog with edittext start
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.verification_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@MainActivity)
                    .setView(mDialogView)

                //show dialog
                val  mAlertDialog = mBuilder.show()

//                mDialogView.otp.showSoftInputOnFocus = false



                //login button click of custom layout
                mDialogView.otp_enter.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()

                    //get text from EditTexts of custom layout
                    verification_code = mDialogView.otp.text.toString()

                    // call authenticate to verify OTP
                    authenticate()




                }
                //cancel button click of custom layout
                mDialogView.otp_x_btn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
//                    closeKeyBoard()
                }

                // dialog with editText ends

                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
                progress.visibility = View.INVISIBLE
            }

        }
    }


    private fun authenticate () {
        val verifyNo = verification_code

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifyNo)

       linkAccount(credential)


    }

    private fun linkAccount(credential: PhoneAuthCredential){

//        Log.d("test","inlink"+credential.toString())

        // show progress animation start
        progress.visibility = View.VISIBLE
        // show progress animation ends

        mAuth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    Log.d("test", "linkWithCredential:success")

                    // dialog box asking name start

                    //Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.name_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@MainActivity)
                        .setView(mDialogView)
                        .setTitle("ขอชื่อเล่นด้วยครับ")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()
                    //login button click of custom layout
                    mDialogView.dialogLoginBtn.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                        //get text from EditTexts of custom layout
                        name_input = mDialogView.dialogNameEt.text.toString()

                    // add user on to database with phone number

                    // Write user's name to the database

                        val country_code = "+66"
                        val phnNoClean = phnNoTxt.text.toString().replace("-","")
                        val phnNo = country_code+phnNoClean

                    val user = User(phnNo, name_input, global_location_key)

                    database.child("customers").push().setValue(user)

                    val transaction_insert = Transaction(phnNo,0)
                    database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)

                        // set global variables

                        customer_logged_phone = phnNo
                        customer_logged_name = name_input


                    // hide progress animation start
                    progress.visibility = View.INVISIBLE
                    // hide progress animation ends

                        startActivity(Intent(this@MainActivity, customer::class.java))

                    }
                    //cancel button click of custom layout
                    mDialogView.NameDialogCancelBtn.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }

                    // dialog box asking name ends

                } else {
                    Log.w("test", "linkWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, task.exception.toString(),
                        Toast.LENGTH_LONG).show()


                    progress.visibility = View.INVISIBLE

                }


            }



//        mAuth.currentUser!!.linkWithCredential(credential)
//            .addOnCompleteListener {
//                    task: Task<AuthResult> ->
//                if (task.isSuccessful) {
//
//                    Log.d("test","linksuccessful")
//
//                    //add user on to database with phone number
//
//                    // Write user's name to the database
//
////                    val country_code = "+66"
////                    val phnNo = country_code+phnNoTxt.text.toString()
////
////                    val user = User(phnNo, name_input)
////
////                    database.child("customers").push().setValue(user)
////
////                    val transaction_insert = Transaction(phnNo,0)
////                    database.child("transactions").push().setValue(transaction_insert)
//
//
//
//                    // hide progress animation start
//                    progress.visibility = View.INVISIBLE
//                    // hide progress animation ends
//
//                    startActivity(Intent(this, customer::class.java))
//                }
//            }
    }

       // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] < -4) {
            startActivity(Intent(this,dashboard::class.java))

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

    private fun welcome_text(){

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends
        // read setting from database

        var settings_ref  = database.child("settings").child("welcome_text")



        val device_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                welcome_text.text = dataSnapshot.getValue().toString()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error", "welcome text failed", databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Failed to load welcome text.",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }


        settings_ref.addListenerForSingleValueEvent(device_listener)

        // ends database read


    }



    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }



}
