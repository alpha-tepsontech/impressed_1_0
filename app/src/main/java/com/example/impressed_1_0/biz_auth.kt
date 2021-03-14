package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_biz_auth.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.example.impressed_1_0.MyApplication.Companion.global_verified
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.forgot_pw_dialog.*
import kotlinx.android.synthetic.main.forgot_pw_dialog.view.*
import kotlinx.android.synthetic.main.name_dialog.view.*
import kotlinx.android.synthetic.main.name_dialog.view.NameDialogCancelBtn
import kotlinx.android.synthetic.main.name_dialog.view.dialogLoginBtn
import kotlinx.android.synthetic.main.newphone_dialog.view.*
import kotlinx.android.synthetic.main.verification_dialog.view.*

import java.util.concurrent.TimeUnit


class biz_auth : AppCompatActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    // phone verification
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var verification_code = ""
    var verificationId = ""
    var phnClean:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_auth)


        // move view up when soft keyboard appear
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends

        // check if user is already signed in

        if(auth.currentUser !== null){

            signIn()

        }


        // btn_enter
        btn_enter.setOnClickListener {

            //show progressbar

            progressBar.visibility = View.VISIBLE


            // log in user

            if (!email_input.text.isEmpty() && !pw_input.text.isEmpty()) {

                // sign user in
                auth.signInWithEmailAndPassword(email_input.text.toString(), pw_input.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("test", "signinUserWithEmail:success")
                            Toast.makeText(this, "signin_success", Toast.LENGTH_SHORT).show()

                            signIn()


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            //hide progressBar

                            progressBar.visibility = View.INVISIBLE
                        }
                    }


            } else {
                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE

            }


        }

        // btn_enter ends

        // back btn

        back.setOnClickListener {
            finish()
        }
        // back btn ends

        // sign up btn

        signup.setOnClickListener {

            if(pw_input.text.length < 6){


                // Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.confirm_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@biz_auth)
                    .setView(mDialogView)
                    .setTitle("กรุณาเลือก password ใหม่")
                //show dialog
                val  mAlertDialog = mBuilder.show()
                mDialogView.confirm_title.text = "error code:"
                mDialogView.confirm_body.text = "ความยาว password อย่างน้อย 6 ตัวอักษร"
                mDialogView.confirm_next.text = "ลองอีกครั้ง"
                //login button click of custom layout
                mDialogView.confirm_next.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()

                    // hide keyboard

                    val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)



                }
                //cancel button click of custom layout
                mDialogView.confirm_cancel.visibility = View.GONE

                // dialog with editText ends

                progressBar.visibility = View.INVISIBLE

            } else if (!email_input.text.isEmpty() && !pw_input.text.isEmpty()) {


                // verify phone number
                phoneDialog()


            } else {
                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()

                //hide progressBar

                progressBar.visibility = View.INVISIBLE

            }

        }

        forgot_pass.setOnClickListener {

            // dialog with edittext start
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.forgot_pw_dialog,null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this@biz_auth)
                .setView(mDialogView)
                .setTitle("")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.dialog_send.setOnClickListener {

                var dialog_email_string = mDialogView.dialog_email.text.toString()

                auth!!.sendPasswordResetEmail(dialog_email_string)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Success!! please follow instruction sent to "+dialog_email_string, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(
                                baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }





                //dismiss dialog
                mAlertDialog.dismiss()





            }
            //cancel button click of custom layout
            mDialogView.forgot_pw_cancel_btn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

            // dialog with editText ends



        }

    }

    private fun signIn () {

        //hide progressBar

        progressBar.visibility = View.INVISIBLE

        startActivity(Intent(this, dashboard::class.java))

    }


    private fun phoneDialog(){

        // dialog box asking phone number start

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.newphone_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_auth)
            .setView(mDialogView)
            .setTitle("ขอเบอร์โทรติดต่อด้วยครับ")


        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)

        // set text

        val phnNoTxt = mDialogView.newPhoneEt

        val log_in_btn = mDialogView.dialogLoginBtn

        log_in_btn.setEnabled(false)



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
                    log_in_btn.setBackgroundColor(resources.getColor(R.color.colorBackground))

                }else{
                    log_in_btn.setEnabled(false)
                    log_in_btn.setBackgroundColor(resources.getColor(R.color.colorDisabled))

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
        mDialogView.dialogLoginBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()

            // hide keyboard

            val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)


            val country_code = "+66"
            val phnNoClean = phnNoTxt.text.toString().replace("-","")
            phnClean = country_code+phnNoClean



            OTPprep()



        }
        //cancel button click of custom layout
        mDialogView.NameDialogCancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            // hide keyboard

            val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        // dialog box asking phone ends
    }


    // ask if ready to get OTP
    private fun OTPprep(){

        // keyboard prevention

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.confirm_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_auth)
            .setView(mDialogView)
            .setTitle("ยืนยันตัวตน")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        // disable touch outside

        mAlertDialog.setCanceledOnTouchOutside(false)


        mDialogView.confirm_body.text = "ประทับใจจะส่งรหัส OTP ไปที่เบอร์โทรศัพท์ที่ลงทะเบียนไว้ พร้อมแล้วกดส่งรหัสเลยครับ"
        mDialogView.confirm_next.text = "ส่งรหัส OTP"
        //login button click of custom layout
        mDialogView.confirm_next.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()

            // show progress bar

            progressBar.visibility = View.VISIBLE

            phnVerify()


        }
        //cancel button click of custom layout
        mDialogView.confirm_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }

    private fun phnVerify(){

       val phnNo = phnClean

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
                progressBar.visibility = View.INVISIBLE
            }


            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {

                progressBar.visibility = View.INVISIBLE

                // dialog with edittext start
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.verification_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@biz_auth)
                    .setView(mDialogView)
                    .setTitle("รหัสถูกส่งไปที่ : "+ phoneFormat(phnClean))

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

                    // show progress bar

                    progressBar.visibility = View.VISIBLE

                    // hide keyboard

                    val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

                    val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                }

                // dialog with editText ends

                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
            }

        }
    }
// verification callback ends

    private fun authenticate () {
        val verifyNo = verification_code

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifyNo)

        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // progress bar

                    progressBar.visibility = View.VISIBLE

                    // set OTP gate keeper

                    global_verified = true
                    // Sign in success, update UI with the signed-in user's information

                    val credential = EmailAuthProvider.getCredential(email_input.text.toString(),
                        pw_input.text.toString())


                    linkEmail(credential)
                } else {



                    // if OTP auth failed
                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@biz_auth)
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

                        val imm = this@biz_auth.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                        OTPprep()


                    }
                    //cancel button click of custom layout
                    mDialogView.confirm_cancel.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }

                    // dialog with editText ends

                    progressBar.visibility = View.INVISIBLE


                }
                }
            }



    private fun linkEmail(credential: AuthCredential){

        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    signup()


                } else {
                    // if OTP auth failed
                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@biz_auth)
                        .setView(mDialogView)
                        .setTitle("sign in ไม่สำเร็จ")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()
                    mDialogView.confirm_title.text = "error code:"
                    mDialogView.confirm_body.text = task.exception?.message
                    mDialogView.confirm_next.visibility = View.GONE
                    mDialogView.confirm_cancel.text = "ย้อนกลับ"

                    //cancel button click of custom layout
                    mDialogView.confirm_cancel.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }

                    // dialog with editText ends

                }

            }

    }

    private fun signup(){
        // show progress bar

        progressBar.visibility = View.VISIBLE

        // to unlink email use "password"
        auth.currentUser!!.unlink("phone")

            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

                    // read standard promo from settings table





                    // write to database

                    var biz_uid = auth.currentUser!!.uid

                    val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


                    var location_key = database.push().key.toString()


                    var device_info = Devices(deviceID,deviceID,location_key)

                    database.child("biz_owners").child(biz_uid).child("devices").push().setValue(device_info)

                    var user_info = UserData(phnClean,1)
                    database.child("biz_owners").child(biz_uid).child("info").setValue(user_info)

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
                            for (dst in dataSnapshot.child("tresholds").children) {
                                val treshold_key = dst.key.toString()

                                val treshold_value: Int? = dst.getValue(Int::class.java)

                                database.child("biz_owners").child(biz_uid).child("tresholds").child(treshold_key).setValue(treshold_value)

                            }

                                Log.d("test",dataSnapshot.toString())

                            for (dsh in dataSnapshot.children) {

                                if(dsh.key == "heart_life"){

                                    val heart_life_value: Int? = dsh.getValue(Int::class.java)
                                    var location_info = Location("สาขาแรก",100,heart_life_value)

                                    Log.d("test","locationKey is  "+location_key.toString())

                                    database.child("biz_owners").child(biz_uid).child("locations").child(location_key).setValue(location_info)
                                }





                            }

                            for (dsp1 in dataSnapshot.child("pricing").child("tier_1").children) {
                                val tier1_key = dsp1.key.toString()

                                val tier1_value: String? = dsp1.getValue(String::class.java)

                                database.child("biz_owners").child(biz_uid).child("pricing").child("tier_1").child(tier1_key).setValue(tier1_value)

                            }
                            for (dsp2 in dataSnapshot.child("pricing").child("tier_2").children) {
                                val tier2_key = dsp2.key.toString()

                                val tier2_value: String? = dsp2.getValue(String::class.java)

                                database.child("biz_owners").child(biz_uid).child("pricing").child("tier_2").child(tier2_key).setValue(tier2_value)

                            }
                            for (dsp3 in dataSnapshot.child("pricing").child("tier_3").children) {
                                val tier3_key = dsp3.key.toString()

                                val tier3_value: String? = dsp3.getValue(String::class.java)

                                database.child("biz_owners").child(biz_uid).child("pricing").child("tier_3").child(tier3_key).setValue(tier3_value)

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


                    settings_ref.addListenerForSingleValueEvent(device_listener)

                    // ends database read



                    startActivity(Intent(this, dashboard::class.java))


                } else {
                    // if OTP auth failed
                    // Inflate the dialog with custom view
                    val mDialogView = LayoutInflater.from(this@biz_auth).inflate(R.layout.confirm_dialog,null)

                    //AlertDialogBuilder
                    val mBuilder = AlertDialog.Builder(this@biz_auth)
                        .setView(mDialogView)
                        .setTitle("sign in ไม่สำเร็จ")
                    //show dialog
                    val  mAlertDialog = mBuilder.show()
                    mDialogView.confirm_title.text = "error code:"
                    mDialogView.confirm_body.text = task.exception?.message
                    mDialogView.confirm_next.visibility = View.GONE
                    mDialogView.confirm_cancel.text = "ย้อนกลับ"

                    //cancel button click of custom layout
                    mDialogView.confirm_cancel.setOnClickListener {
                        //dismiss dialog
                        mAlertDialog.dismiss()
                    }

                    // dialog with editText ends

                    progressBar.visibility = View.INVISIBLE
                }


                }
            }




//    private fun old_signup(){
//
//        // sign user up
//        auth.createUserWithEmailAndPassword(
//            email_input.text.toString(),
//            pw_input.text.toString()
//        )
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d("test", "createUserWithEmail:success")
//                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
//
//                    // read standard promo from settings table
//
//
//
//
//
//                    // write to database
//
//                    var biz_uid = auth.currentUser!!.uid
//
//                    val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//
//
//                    var location_key = database.push().key.toString()
//
//                    var location_info = Location("สาขาแรก",100)
//
//                    database.child("biz_owners").child(biz_uid).child("locations").child(location_key).setValue(location_info)
//
//                    var device_info = Devices(deviceID,deviceID,location_key)
//
//                    database.child("biz_owners").child(biz_uid).child("devices").push().setValue(device_info)
//
//                    var user_info = UserData(phnNoTxt.text.toString(),0)
//                    database.child("biz_owners").child(biz_uid).child("info").setValue(user_info)
//
//                   // read setting from database
//
//                    var settings_ref  = database.child("settings").child("standard_promo")
//
//
//
//                    val device_listener = object : ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//
//                            for (ds in dataSnapshot.children) {
//                                val standard_promo_key = ds.key
//                                val standard_promo_price: Int? = ds.getValue(Int::class.java)
//                                var promo_key = database.push().key.toString()
//
//                                var promo_insert = Promos(standard_promo_key,standard_promo_price)
//
//                                database.child("biz_owners").child(biz_uid).child(location_key).child("promos").child(promo_key).setValue(promo_insert)
//
//                            }
//                        }
//
//                        override fun onCancelled(databaseError: DatabaseError) {
//                            // Getting Post failed, log a message
//                            Log.w("error", "loadPost:onCancelled", databaseError.toException())
//                            // [START_EXCLUDE]
//                            Toast.makeText(baseContext, "Failed to load post.",
//                                Toast.LENGTH_SHORT).show()
//                            // [END_EXCLUDE]
//                        }
//                    }
//
//
//                    settings_ref.addListenerForSingleValueEvent(device_listener)
//
//               // ends database read
//
//
//
//                    startActivity(Intent(this, dashboard::class.java))
//
//
//                } else {
//                    // If sign in fails, display a message to the user.
//
//                    Log.w("test", "createUserWithEmail:failure", task.exception)
//                    Toast.makeText(
//                        baseContext, task.exception.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()
//
//                    progressBar.visibility = View.INVISIBLE
//                }
//            }
//    }





}
