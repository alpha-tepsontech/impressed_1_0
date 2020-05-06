package com.example.impressed_1_0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.auth.PhoneAuthCredential

import android.util.Log
import android.app.AlertDialog
import android.content.DialogInterface

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthProvider
//import kotlinx.android.synthetic.main.activity_phone_authentication.*
import java.util.concurrent.TimeUnit


import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // firebase auth setup

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log_in_btn.text = "enter"
        welcome_text.text = "this is mother fucking welcome text MF!!"

        // firebase auth
        mAuth = FirebaseAuth.getInstance()

        log_in_btn.setOnClickListener {
                view: View? -> progress.visibility = View.VISIBLE
            verify ()


//            if (mAuth.currentUser == null){
//                Toast.makeText(this, "button pressed :)", Toast.LENGTH_LONG).show()
//            startActivity(Intent(this, customer::class.java))
//            }
        }
    }

    private fun verificationCallbacks () {
        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                progress.visibility = View.INVISIBLE
                signIn(credential)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCodeSent(verfication: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
                progress.visibility = View.INVISIBLE
            }

        }
    }

    private fun verify () {

        verificationCallbacks()

        val phnNo = phnNoTxt.text.toString()

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
//                    toast("Logged in Successfully :)")
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
