package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_biz_auth.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.util.concurrent.TimeUnit

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
                            Log.w("test", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, task.exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }


            } else {
                Log.d("test", "empty_editText")
                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()

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
                auth.createUserWithEmailAndPassword(
                    email_input.text.toString(),
                    pw_input.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("test", "createUserWithEmail:success")
                            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("test", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


            } else {
                Log.d("test", "empty_editText")
                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()

            }

        }
    }

    private fun signIn () {


                    //hide progressBar

                    progressBar.visibility = View.INVISIBLE

                    startActivity(Intent(this, MainActivity::class.java))

    }


}
