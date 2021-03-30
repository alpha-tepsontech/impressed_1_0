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
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.example.impressed_1_0.MyApplication.Companion.global_verified
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_biz_redeem.*
import kotlinx.android.synthetic.main.activity_customer_redeem.*
import kotlinx.android.synthetic.main.activity_customer_redeem.otpText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.redeem.view.*
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.util.concurrent.TimeUnit

// set sensor vars
private var mSensorManager : SensorManager?= null
private var mAccelerometer : Sensor?= null



// set sensor vars ends

class biz_redeem : AppCompatActivity(), SensorEventListener {

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    // phone verification
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verification_code = ""
    var verificationId = ""

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

        // firebase auth
        mAuth = FirebaseAuth.getInstance()
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


        // hide keyboard

        val imm = this@biz_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

        // set golden heart

        if(intent.getStringExtra("biz_coupon_key") != null){
            mDialogView.redeem_heart.setImageResource(R.drawable.g_heart)
        }

        //login button click of custom layout

        // button click of custom layout
        mDialogView.redeem_btn.setOnClickListener {
            // dismiss dialog
            mAlertDialog.dismiss()

            if(global_verified == true){

                if(intent.getStringExtra("biz_coupon_key") != null){
                    coupon_redeem()
                }else redeem()

            }else{
                OTPprep()
            }
        }

        //cancel button click of custom layout
        mDialogView.redeem_cancel.setOnClickListener {

            //dismiss dialog
            startActivity(Intent(this,biz_dashboard::class.java))
        }

        // dialog with editText ends



    }

    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] > 4) {
            val redeem_name = intent.getStringExtra("biz_redeem_name")
            val promo_worth = intent.getStringExtra("biz_promoWorth")




            val intent = Intent(this,customer_redeem::class.java)
            intent.putExtra("redeem_name",redeem_name)
            intent.putExtra("promoWorth",promo_worth)
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


    private fun OTPprep(){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_redeem).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_redeem)
            .setView(mDialogView)
            .setTitle("ยืนยันตัวตน")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.confirm_body.text = "ประทับใจจะส่งรหัส OTP ไปที่เบอร์โทรศัพท์ที่ลงทะเบียนไว้ พร้อมแล้วกดส่งรหัสเลยครับ"
        mDialogView.confirm_next.text = "ส่งรหัส OTP"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            phnVerify()


        }
        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            //dismiss dialog
            finish()
        }

        // dialog with editText ends

    }


    private fun phnVerify(){

        val phnNo = customer_logged_phone!!
        verificationCallbacks()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phnNo,
            30,
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
//                progress.visibility = View.INVISIBLE
            }


            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {


                // hide progress bar

                progress.visibility = View.INVISIBLE
                otpText.visibility = View.INVISIBLE

                // dialog with edittext start
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@biz_redeem).inflate(R.layout.verification_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@biz_redeem)
                    .setView(mDialogView)
                    .setTitle("รหัสถูกส่งไปที่ : "+ phoneFormat(customer_logged_phone!!))

                //show dialog
                val  mAlertDialog = mBuilder.show()

                // disable touch outside

                mAlertDialog.setCanceledOnTouchOutside(false)

                // start countdown for resend button

                object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {

                        mDialogView.otp_resend.text = "resend OTP ("+(millisUntilFinished / 1000).toString()+")"
                    }
                    override fun onFinish() {
                        mDialogView.otp_resend.text = "resend OTP"
                        mDialogView.otp_resend.isEnabled = true
                        mDialogView.otp_resend.setBackgroundColor(resources.getColor(R.color.colorBackground))
                    }
                }.start()

                // countdown button ends

                // enable btn if text field is not empty - start

                mDialogView.otp.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(
                        s: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) { if (s.toString().trim { it <= ' ' }.length == 6){
                        mDialogView.otp_enter.setEnabled(true)

                    }else{
                        mDialogView.otp_enter.setEnabled(false)

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
                    // hide keyboard

                    val imm = this@biz_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                }

                // dialog with editText ends

                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
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
        otpText.visibility = View.VISIBLE
        // show progress animation ends

        mAuth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // show progress animation start
                    progress.visibility = View.INVISIBLE
                    otpText.visibility = View.INVISIBLE

                    // unlink phone number
                    unlink("phone")

                    // remember that user has already gone through OTP

                    global_verified = true

                    if(intent.getStringExtra("biz_coupon_key") != null){
                        coupon_redeem()
                    }else redeem()




                    // dialog box asking name ends

                } else {
                    // if OTP auth failed
                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@biz_redeem).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@biz_redeem)
                        .setView(mDialogView)
                        .setTitle("ตรวจสอบ OTP ไม่สำเร็จ")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()
                    mDialogView.confirm_title.text = "error code:"
                    mDialogView.confirm_body.text = task.exception?.message
                    mDialogView.confirm_next.text = "ลองอีกครั้ง"
                    //login button click of custom layout
                    mDialogView.confirm_next.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()

                        // hide keyboard

                        val imm = this@biz_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                        phnVerify()


                    }
                    //cancel button click of custom layout
                    mDialogView.confirm_cancel.setOnClickListener {
                        //dismiss dialog
                        startActivity(Intent(this,customer::class.java))
                    }

                    // dialog with editText ends

                }


            }
           }




    private fun redeem(){
        // write tx to database
        val heartInsert = -intent.getStringExtra("biz_promoWorth").toInt()

        val blank:Float = 0.0f

        val transaction_insert = Transaction(customer_logged_phone,heartInsert,blank,blank,"redeem")

        database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)

        otpText.text = "redeem in progress"





        //dismiss dialog
        startActivity(Intent(this,biz_dashboard::class.java))
    }

    private fun coupon_redeem(){
        // write tx to database
        val couponKey = intent.getStringExtra("biz_coupon_key")

        val couponAmount = intent.getStringExtra("biz_promoWorth").toInt()

        val blank:Float = 0.0f

        val transaction_insert = Coupons_tx(customer_logged_phone,0,-couponAmount,blank,blank,"coupon_redeem",couponKey)

        database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)

        otpText.text = "coupon redeem in progress"



        //dismiss dialog
        startActivity(Intent(this,biz_dashboard::class.java))
    }


    private fun unlink(providerId: String) {

        // [START auth_unlink]
        mAuth.currentUser!!.unlink(providerId)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {


                }
            }
        // [END auth_unlink]
    }

}
