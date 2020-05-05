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


import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // firebase auth setup
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        log_in_btn.text = "enter"
        welcome_text.text = "this is mother fucking welcome text MF!!"

        // firebase auth
        mAuth = FirebaseAuth.getInstance()

        log_in_btn.setOnClickListener {
            if (mAuth.currentUser != null){
               print("test")
            startActivity(Intent(this, customer::class.java))
            }
        }


    }

}
