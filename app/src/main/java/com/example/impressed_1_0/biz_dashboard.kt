package com.example.impressed_1_0

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_biz_dashboard.*

// set up sensor events
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.Sensor
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_name
import com.example.impressed_1_0.MyApplication.Companion.customer_logged_phone
import com.example.impressed_1_0.MyApplication.Companion.global_location_key
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_biz_dashboard.log_out_btn
import kotlinx.android.synthetic.main.activity_biz_dashboard.total_input
import kotlinx.android.synthetic.main.total_ent_dialog.view.*
import kotlinx.android.synthetic.main.tx_del_dialog.view.*


// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends





class biz_dashboard : AppCompatActivity() , SensorEventListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    var heartWorth:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_dashboard)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // gravity sensor setup
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        // gravity sensor setup ends

        // enable btn if text field is not empty - start

        total_ent.setEnabled(false)

        total_input.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString().trim { it <= ' ' }.length == 0) {
                    total_ent.setEnabled(false)
                } else {
                    total_ent.setEnabled(true)
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




        // set elements

        if(auth.currentUser !== null){
            biz_email_display.text = auth.currentUser!!.email
        }

        customer_phone.text = customer_logged_phone
        customer_name.text = customer_logged_name

        // initialize_database_ref
        database = Firebase.database.reference
        // init ends


        // set elements
        // get device data from database

        var biz_uid = auth.currentUser!!.uid
//
//        var device_ref  = database.child("biz_owners").child(biz_uid).child("devices").orderByChild("locationKey").equalTo(global_location_key)
//
//        val device_listener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//
//                for (ds in dataSnapshot.children) {
//                    val deviceName = ds.child("deviceName").getValue()
//                    biz_device_display.text = deviceName.toString()
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("error", "loadPost:onCancelled", databaseError.toException())
//                // [START_EXCLUDE]
//                Toast.makeText(baseContext, "Failed to load post.",
//                    Toast.LENGTH_SHORT).show()
//                // [END_EXCLUDE]
//            }
//        }
//        device_ref.addValueEventListener(device_listener)
        // database ends


        // get location data from database

        var location_ref  = database.child("biz_owners").child(biz_uid).child("locations").child(global_location_key.toString())

        val location_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val location_info = dataSnapshot.getValue()



//                biz_location_display.text = dataSnapshot.child("locationName").getValue().toString()
                heartWorth = dataSnapshot.child("heartWorth").getValue().toString().toInt()
                heart_display.text = heartWorth.toString()

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
        location_ref.addValueEventListener(location_listener)
        // database ends






        // get promos

        promos_database_read()


        // get tx records

        tx_record_read()

        // read values from database

        tx_database_read()




        // custom keyboard start
        // disable softkeyboard on focus

        total_input.setShowSoftInputOnFocus(false);

        // diasble softkeyboard on focus ends


        letter9.setOnClickListener {

            total_input.append("9")

        }
        letter8.setOnClickListener {

            total_input.append("8")

        }
        letter7.setOnClickListener {

            total_input.append("7")

        }
        letter6.setOnClickListener {

            total_input.append("6")

        }
        letter5.setOnClickListener {

            total_input.append("5")

        }

        letter4.setOnClickListener {

            total_input.append("4")

        }
        letter3.setOnClickListener {

            total_input.append("3")

        }
        letter2.setOnClickListener {

            total_input.append("2")

        }
        letter1.setOnClickListener {

            total_input.append("1")

        }
        letter0.setOnClickListener {

            total_input.append("0")

        }
        decimal.setOnClickListener {

            total_input.append(".")

        }

        total_del.setOnClickListener {
            //delete last digit
            var droped = total_input.text.dropLast(1)
            //set text
            total_input.setText(droped)
            //put cursor to the end
            total_input.setSelection(total_input.text.length);



        }

        clear.setOnClickListener {
            //set text
            total_input.setText("")



        }

        total_ent.setOnClickListener {



            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.total_ent_dialog,null)

            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this@biz_dashboard)
                .setView(mDialogView)
                .setTitle("")
            //show dialog
            val  mAlertDialog = mBuilder.show()

            mDialogView.total_amount.text = total_input.text


            val base_heart = kotlin.math.floor(total_input.text.toString().toFloat()/heartWorth.toString().toFloat())

            val upsale = heartWorth-total_input.text.toString().toFloat()%heartWorth.toString().toFloat()

            mDialogView.heart_amount.text = base_heart.toString()

            mDialogView.upsale_amount.text = upsale.toString()



            //login button click of custom layout
            mDialogView.total_save_btn.setOnClickListener {

                // tx write to database

                var totalInput = total_input.text.toString().toFloat()
            var heartInsert = kotlin.math.floor(totalInput/heartWorth.toFloat()).toInt()
            val transaction_insert = Transaction(customer_logged_phone,heartInsert,totalInput)

            database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)

            total_input.text.clear()


                //dismiss dialog
                mAlertDialog.dismiss()

                //restart activity
//            finish();
//            startActivity(getIntent());

            }
            //cancel button click of custom layout
            mDialogView.total_x_btn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

            mDialogView.upsale_btn.setOnClickListener {

                //set hint

                val total_base = total_input.text.toString()

                total_input.text.clear()
                total_input.setHint("ยอดเดิม $total_base บาทได้ $base_heart ดวง ซื้อเพ่ิม $upsale บาทได้เพิ่ม 1 ดวง")


                //dismiss dialog
                mAlertDialog.dismiss()
            }

            // dialog with editText ends

        }

        // custom keyboard ends

        log_out_btn.setOnClickListener {
              customer_logged_phone = ""
              customer_logged_name = ""

            unlink("phone")

            startActivity(Intent(this,dashboard::class.java))

        }


    }
    // gravity sensor code


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values[0] > 8) {
            startActivity(Intent(this,customer::class.java))


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


    private fun unlink(providerId: String) {

        // [START auth_unlink]
        auth.currentUser!!.unlink(providerId)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    }
            }
        // [END auth_unlink]
    }


    private fun promos_database_read(){

        //read data

        var biz_uid = auth.currentUser!!.uid

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("promos").orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)

                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)



                    val promo_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.biz_promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()
                    biz_promo_frame.addView(promo_set)


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        promos_ref.addListenerForSingleValueEvent(promos_listener)
        // database ends




    }

    private fun tx_record_read(){

        //read data


        var tx_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild(customer_logged_phone.toString()).limitToLast(4)

        val tx_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                tx_scroll_view.removeAllViews()



                for (ds in dataSnapshot.children) {

                    var txAmount = ds.child("amount").getValue(Float::class.java)

                    var txDay = ds.child("time").child("date").getValue(Int::class.java)
                    var txMonth = ds.child("time").child("month").getValue(Int::class.java)
                    var txYear_raw = ds.child("time").child("year").getValue(Int::class.java)
                    var txYear = 1900+ txYear_raw!!

                    var txHour = ds.child("time").child("hours").getValue(Int::class.java)
                    var txMinute = ds.child("time").child("minutes").getValue(Int::class.java)





                    val tx_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.tx_record,null)
                    val tx_holder = tx_set.findViewById<TextView>(R.id.tx_record)
                    val tx_day_holder = tx_set.findViewById<TextView>(R.id.tx_day)
                    val tx_month_holder = tx_set.findViewById<TextView>(R.id.tx_month)
                    val tx_year_holder = tx_set.findViewById<TextView>(R.id.tx_year)
                    val tx_hour_holder = tx_set.findViewById<TextView>(R.id.tx_hour)
                    val tx_minute_holder = tx_set.findViewById<TextView>(R.id.tx_minute)

                    tx_holder.text = txAmount.toString()
                    tx_day_holder.text = txDay.toString()
                    tx_month_holder.text = txMonth.toString()
                    tx_year_holder.text = txYear.toString()
                    tx_hour_holder.text = txHour.toString()
                    tx_minute_holder.text = txMinute.toString()


                    tx_scroll_view.addView(tx_set)

                    // setup tx_del
                    var txKey = ds.key.toString()


                    val tx_del_btn = tx_set.findViewById<ImageButton>(R.id.tx_del)

                    tx_del_btn.setOnClickListener{

                        if (txKey != null) {
                            tx_delete(txKey,
                                txAmount!!,
                                txDay!!,
                                txMonth!!,
                                txYear!!,
                                txHour!!,
                                txMinute!!
                            )
                        }

                    }


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        tx_ref.addValueEventListener(tx_listener)
        // database ends




    }

    private fun tx_delete(txKey:String,txAmount:Float,txDay:Int,txMonth:Int,txYear:Int,txHour:Int,txMinute:Int){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.tx_del_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.tx_del_amount.text = txAmount.toString()
        mDialogView.tx_del_day.text = txDay.toString()
        mDialogView.tx_del_month.text = txMonth.toString()
        mDialogView.tx_del_year.text = txYear.toString()
        mDialogView.tx_del_hour.text = txHour.toString()
        mDialogView.tx_del_minute.text = txMinute.toString()

        //login button click of custom layout
        mDialogView.tx_del_btn.setOnClickListener {

            // delete from database

            database.child("transactions").child(global_location_key.toString()).child(txKey).removeValue()


            //dismiss dialog
            mAlertDialog.dismiss()

            //restart activity
//            finish();
//            startActivity(getIntent());

        }
        //cancel button click of custom layout
        mDialogView.tx_del_CancelBtn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

        // dialog with editText ends

    }


    private fun tx_database_read(){

        //read data

        var tx_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(customer_logged_phone.toString())

        val tx_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var amount_sum = 0.00F
                var heart_sum = 0
                for (ds in dataSnapshot.children) {

                    amount_sum += ds.child("amount").getValue(Float::class.java)!!
                    heart_sum += ds.child("heartBank").getValue(Int::class.java)!!

                }

                customer_total.text = amount_sum.toString()
                customer_heart.text = heart_sum.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }

        tx_ref.addValueEventListener(tx_listener)
        // database ends

    }

}
