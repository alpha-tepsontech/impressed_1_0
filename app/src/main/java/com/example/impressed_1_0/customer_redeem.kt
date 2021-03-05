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
import com.example.impressed_1_0.MyApplication.Companion.global_verified
import com.firebase.ui.auth.ui.InvisibleActivityBase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_customer_redeem.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.redeem.view.*
import kotlinx.android.synthetic.main.verification_dialog.*
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.util.concurrent.TimeUnit


// set sensor vars
private var mSensorManager : SensorManager?= null
private var mAccelerometer : Sensor?= null


// set sensor vars ends

class customer_redeem : AppCompatActivity(), SensorEventListener {

    // firebase auth setup

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""
    var verification_code = ""






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

        // firebase auth
        mAuth = FirebaseAuth.getInstance()

        // start OTP verification process

        if(global_verified == true){

            redeem_ready()

        }else {

            OTPprep()


        }



    }
    // ask if ready to get OTP
    private fun OTPprep(){


            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this@customer_redeem).inflate(R.layout.confirm_dialog,null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this@customer_redeem)
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
                sendOTP()


            }
            //cancel button click of custom layout
            mDialogView.confirm_cancel.setOnClickListener {
                //dismiss dialog
                startActivity(Intent(this,customer::class.java))
            }

            // dialog with editText ends

    }
    private fun sendOTP(){

        verificationCallbacks()
        val phnNo = customer_logged_phone!!
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


            }


            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {
                // dialog with edittext start
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@customer_redeem).inflate(R.layout.verification_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@customer_redeem)
                    .setView(mDialogView)
                    .setTitle("รหัสถูกส่งไปที่ : "+ phoneFormat(customer_logged_phone!!))


                //show dialog
                val  mAlertDialog = mBuilder.show()

                // hide progress bar

                progress.visibility = View.INVISIBLE
                otpText.visibility = View.INVISIBLE


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

                // resend btn code

                mDialogView.otp_resend.setOnClickListener {

                    //restart activity
            finish()
            startActivity(getIntent())
                }

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

                    // hide keyboard

                    val imm = this@customer_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

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

                    val imm = this@customer_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }

                mDialogView.otp_x_btn.setOnClickListener {
                    //dismiss dialog
                    finish()
//                    hideKeyboard()
                    val imm = this@customer_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }

                // disable touch outside

                mAlertDialog.setCanceledOnTouchOutside(false)
                // dialog with editText ends

                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
//                progress.visibility = View.INVISIBLE
            }

        }
    }

    // verification callback ends


    private fun authenticate () {

        progress.visibility = View.VISIBLE
        otpText.visibility = View.VISIBLE
        val verifyNo = verification_code

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifyNo)

        linkAccount(credential)


    }

    private fun linkAccount(credential: PhoneAuthCredential){

//        Log.d("test","inlink"+credential.toString())

        // show progress animation start
//        progress.visibility = View.VISIBLE
        // show progress animation ends

        mAuth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    redeem_ready()

                } else {

                    // if OTP auth failed
                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@customer_redeem).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@customer_redeem)
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

                        val imm = this@customer_redeem.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                        sendOTP()


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


    // redeem ready pop up dialog

    private fun redeem_ready(){

        progress.visibility = View.INVISIBLE
       otpText.visibility = View.INVISIBLE


        // unlink phone number
        unlink("phone")

        // prep for flip
        global_verified = true

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
