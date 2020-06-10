package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_biz_auth.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.forgot_pw_dialog.*
import kotlinx.android.synthetic.main.forgot_pw_dialog.view.*

import java.util.concurrent.TimeUnit

// start data class
data class Location(
    var locationName: String? = "",
    var heartWorth: Int? = 0
)
// ends

// start data class
data class Devices(
    var deviceID: String? = "",
    var deviceName: String? = "",
    var locationKey:String? = ""
)
// ends


class biz_auth : AppCompatActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    // firebase realtime database setup
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_auth)


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

                // sign user up
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
                Log.d("test", "empty_editText")
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

            if (!email_input.text.isEmpty() && !pw_input.text.isEmpty()) {

                // show progressbar
                progressBar.visibility = View.VISIBLE
                // sign user up
                signUp()


            } else {
                Log.d("test", "empty_editText")
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

    private fun signUp(){

        // sign user up
        auth.createUserWithEmailAndPassword(
            email_input.text.toString(),
            pw_input.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("test", "createUserWithEmail:success")
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

                    // write to database

                    var biz_uid = auth.currentUser!!.uid

                    val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


                    var location_key = database.push().key.toString()

                    var location_info = Location("สาขาแรก",100)

                    database.child("biz_owners").child(biz_uid).child("locations").child(location_key).setValue(location_info)

                    var device_info = Devices(deviceID,"เครื่องแรก",location_key)

                    database.child("biz_owners").child(biz_uid).child("devices").push().setValue(device_info)


                    startActivity(Intent(this, dashboard::class.java))


                } else {
                    // If sign in fails, display a message to the user.

                    Log.w("test", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, task.exception.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                    progressBar.visibility = View.INVISIBLE
                }
            }
    }


}
