package com.example.impressed_1_0

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_launcher.*
import android.widget.ImageButton
import kotlinx.android.synthetic.main.verification_dialog.view.*



import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase

// set global val

class MyApplication: Application() {
    companion object {
        var global_main_activity = true
        var customer_logged_name : String? = ""
        var customer_logged_phone: String? = ""
        var global_customer = true
    }

}
// set global val ends


class launcher : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        // custom keyboard start
        // disable softkeyboard on focus

            phone_input.setShowSoftInputOnFocus(false);

        // diasble softkeyboard on focus ends


        letter9.setOnClickListener {

            phone_input.append("9")

        }
        letter8.setOnClickListener {

            phone_input.append("8")

        }
        letter7.setOnClickListener {

            phone_input.append("7")

        }
        letter6.setOnClickListener {

            phone_input.append("6")

        }
        letter5.setOnClickListener {

            phone_input.append("5")

        }

        letter4.setOnClickListener {

            phone_input.append("4")

        }
        letter3.setOnClickListener {

            phone_input.append("3")

        }
        letter2.setOnClickListener {

            phone_input.append("2")

        }
        letter1.setOnClickListener {

            phone_input.append("1")

        }
        letter0.setOnClickListener {

            phone_input.append("0")

        }

        del.setOnClickListener {
            //delete last digit
            var droped = phone_input.text.dropLast(1)
            //set text
            phone_input.setText(droped)
            //put cursor to the end
            phone_input.setSelection(phone_input.text.length);



        }

            btn_enter.setOnLongClickListener {


            Log.d("test","longClicked")
            true

        }

        // custom keyboard ends

        //send to activity base on rotation

        val rotation = windowManager.defaultDisplay.rotation

        if (rotation == 1 || rotation == 3){

            startActivity(Intent(this,launcher_land::class.java))
        }



    }
}