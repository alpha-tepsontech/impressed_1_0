package com.example.impressed_1_0

//import kotlinx.android.synthetic.main.activity_phone_authentication.*


import android.R.attr.button
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {
    // firebase auth setup

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log_in_btn.text = "enter"
        // disable the btn until input is entered
        log_in_btn.setEnabled(false)
        welcome_text.text = "this is mother fucking welcome text MF!!"

        // firebase auth
        mAuth = FirebaseAuth.getInstance()

        // enable btn if text field is not empty - start

        phnNoTxt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString().trim { it <= ' ' }.length == 0) {
                    log_in_btn.setEnabled(false)
                } else {
                    log_in_btn.setEnabled(true)
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

                verify ()
            }else{
//                    view: View? -> progress.visibility = View.INVISIBLE
// dialog box code start
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(this)

                // set message of alert dialog
                dialogBuilder.setMessage("ใส่เบอร์โทรได้เลยครับ")
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




//            if (mAuth.currentUser == null){
//                Toast.makeText(this, "button pressed :)", Toast.LENGTH_LONG).show()
//            startActivity(Intent(this, customer::class.java))
//            }
        }
    }

    private fun verificationCallbacks () {
        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("test","Vcompleted")
                progress.visibility = View.INVISIBLE
                signIn(credential)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Log.d("test","Vfailed")
                progress.visibility = View.INVISIBLE
            }





            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {
                Log.d("test","Vcodesent")

                // dialog box code start
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(this)

                // set message of alert dialog
                dialogBuilder.setMessage("ใส่ OTP ได้เลยคร้าบ")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        //dialog, id ->finish()
                            dialog, id -> startActivity(Intent(this, customer::class.java))
                    })
                    // negative button text and action
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Hello MFing world")
                // show alert dialog
                alert.show()

// dialog box code ends


                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
                progress.visibility = View.INVISIBLE
            }

        }
    }

    private fun verify () {
        Log.d("test","verify")
        //Thread.sleep(2_000)

        verificationCallbacks()
        val country_code = "+66"
        val phnNo = country_code+phnNoTxt.text.toString()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phnNo,
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )
    }

    private fun signIn (credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->
                if (task.isSuccessful) {
//                    Toast("Logged in Successfully :)")
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
    }

    private fun authenticate () {

        val verifiNo = verifiTxt.text.toString()

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifiNo)

        signIn(credential)

    }


}
