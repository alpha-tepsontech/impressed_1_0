package com.example.impressed_1_0

//import kotlinx.android.synthetic.main.activity_phone_authentication.*


// import elements from verification_dialog view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.verification_dialog.view.*
import java.util.concurrent.TimeUnit


// [START User_class]
data class User(
    var phone: String? = "",
    var name: String? = "",
    var location: String? = "",
    var heart: Int? = 0
)
// [END user_class]


class MainActivity : AppCompatActivity() {
    // firebase auth setup

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""
    var verification_code = ""
    var name_input = ""

    // firebase realtime database setup
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        log_in_btn.text = "enter"
        // disable the btn until input is entered
        log_in_btn.setEnabled(false)
        welcome_text.text = "this is mother fucking welcome text MF!!"


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



        }

        log_in_btn.setOnLongClickListener {


            Log.d("test","longClicked")
            true

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

                // dialog with edittext start
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.verification_dialog,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@MainActivity)
                    .setView(mDialogView)
                    .setTitle("ยืนยันเบอร์โทรศัพท์")
                //show dialog
                val  mAlertDialog = mBuilder.show()
                //login button click of custom layout
                mDialogView.dialogLoginBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                    //get text from EditTexts of custom layout
                    verification_code = mDialogView.dialogNameEt.text.toString()

                    // call authenticate to verify OTP
                    authenticate()


                }
                //cancel button click of custom layout
                mDialogView.dialogCancelBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }

                // dialog with editText ends

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




    private fun authenticate () {

        val verifyNo = verification_code

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifyNo)


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


            // call signIn and pass credential to sign user in
            signIn(credential)




        }
        //cancel button click of custom layout
        mDialogView.dialogCancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }



        // dialog box asking name ends



    }

    private fun signIn (credential: PhoneAuthCredential) {


        // show progress animation start
        progress.visibility = View.VISIBLE
        // show progress animation ends

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->
                if (task.isSuccessful) {



                    //add user on to database with phone number

                    // Write a message to the database

                    val country_code = "+66"
                    val phnNo = country_code+phnNoTxt.text.toString()

                    val user = User(phnNo, name_input)

                    database.child("users").push().setValue(user)

                    Log.d("test", "user-setValue")

                    // hide progress animation start
                    progress.visibility = View.INVISIBLE
                    // hide progress animation ends

                    startActivity(Intent(this, customer::class.java))
                }
            }
    }


    fun getRotation(context: Context): String? {
        val rotation =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                .orientation
        return when (rotation) {
            Surface.ROTATION_0 -> "portrait"
            Surface.ROTATION_90 -> "landscape"
            Surface.ROTATION_180 -> "reverse portrait"
            else -> "reverse landscape"
        }
    }



}
