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
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import kotlinx.android.synthetic.main.activity_biz_dashboard.customer_heart_countdown_display
import kotlinx.android.synthetic.main.activity_biz_dashboard.heart_display
import kotlinx.android.synthetic.main.activity_biz_dashboard.heart_life_display
import kotlinx.android.synthetic.main.activity_biz_dashboard.log_out_btn
import kotlinx.android.synthetic.main.activity_biz_dashboard.total_input
import kotlinx.android.synthetic.main.activity_choices.*
import kotlinx.android.synthetic.main.activity_customer.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.coupon_amount.view.*
import kotlinx.android.synthetic.main.coupon_amount.view.newdevice_cancel
import kotlinx.android.synthetic.main.coupon_amount.view.newdevice_save
import kotlinx.android.synthetic.main.dialog_frame.view.*
import kotlinx.android.synthetic.main.new_device_dialog.view.*
import kotlinx.android.synthetic.main.total_ent_dialog.view.*
import kotlinx.android.synthetic.main.tx_del_dialog.view.*
import org.w3c.dom.Text
import java.lang.Math.abs
import kotlin.math.roundToInt


// set sensor vars
private var mSensorManager : SensorManager ?= null
private var mAccelerometer : Sensor ?= null

// set sensor vars ends


private var sale_base:Float = 0F
private var heart_sum = 0
private var heart_used = 0
private var heart_life:Int = 0
private var heart_exp_warning:Long = 0L
private var coupon_balance = 0






class biz_dashboard : AppCompatActivity() , SensorEventListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
// [END declare_auth]

    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    var heartWorth = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_dashboard)

        // reset heart counter
        heart_sum = 0


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

        customer_phone.text = phoneFormat(customer_logged_phone.toString())
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

                // tier_1 text setup
                for (ds in dataSnapshot.children) {
                    Log.d("test",ds.toString())

                    when(ds.key){
                        "heartWorth" ->{
                            heartWorth = ds.getValue(Long::class.java).toString()
                            heart_display.text = heartWorth
                        }
                        "heartLife" ->{
                            heart_life = ds.getValue(Int::class.java)!!
                            heart_life_display.text = heart_life.toString()
                        }

                    }
                }




//                heartWorth = dataSnapshot.child("heartWorth").getValue(String::class.java)!!
//                heart_display.text = heartWorth
//                heart_life = dataSnapshot.child("heartLife").getValue(Int::class.java)!!.toInt()
//                heart_life_display.text = heart_life.toString()

           }

            override fun onCancelled(databaseError: DatabaseError) {
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Database failed",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }
        location_ref.addValueEventListener(location_listener)
        // database ends




        // read values from database

        tx_database_read()


        // get tx records

        tx_record_read()





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

        coupon_btn.setOnClickListener {

            coupon_selector()



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

            val upsale = heartWorth.toInt()-total_input.text.toString().toFloat()%heartWorth.toString().toFloat()

            mDialogView.heart_amount.text = base_heart.toString()

            mDialogView.upsale_amount.text = upsale.toString()



            //login button click of custom layout
            mDialogView.total_save_btn.setOnClickListener {

                // tx write to database

                var totalInput = total_input.text.toString().toFloat()
            var heartInsert = kotlin.math.floor(totalInput/heartWorth.toFloat()).toInt()
                var upsale_record = 0f
                if (sale_base > 0F && totalInput > sale_base) {
                    upsale_record = totalInput - sale_base
                } else{
                    upsale_record = 0F
                }

                val transaction_insert = Transaction(customer_logged_phone,heartInsert,totalInput,upsale_record,"sale")

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
                total_input.setHint("ยอดเดิม $total_base บาทได้ $base_heart ดวง ซื้อเพิ่ม $upsale บาทได้เพิ่ม 1 ดวง")

                // record sale base

                sale_base = total_base.toFloat()



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
        if (event != null && event.values[0] > 4) {
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



//             check coupon balance for a specific coupon key for the specific phone number





    private fun tx_record_read(){

        //read data


        var tx_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(
            customer_logged_phone.toString()).limitToLast(10)


        val tx_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                tx_scroll_view.removeAllViews()

                for (ds in dataSnapshot.children) {

                    var txAmount = ds.child("amount").getValue(Float::class.java)
                    var txType = ds.child("type").getValue(String::class.java)
                    var txHeart = ds.child("heartBank").getValue(Int::class.java)
                    var txTimeRaw = ds.child("time").getValue(Long::class.java)!!
                    var txCoupon = ds.child("couponBank").getValue(Int::class.java)


                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy '@' HH:mm")
                    val date = java.util.Date(txTimeRaw * 1000)
                    val txTime = sdf.format(date)

                    // set up sale tx

                    val tx_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.tx_record,null)
                    val tx_holder = tx_set.findViewById<TextView>(R.id.tx_record)
                    val tx_time_holder = tx_set.findViewById<TextView>(R.id.tx_time)


                    tx_holder.text = txAmount!!.roundToInt().toString()
                    tx_time_holder.text = txTime.toString()


                    // end


                    // set up redeem tx

                    val red_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.redeem_set,null)
                    val red_holder = red_set.findViewById<TextView>(R.id.red_record)
                    val red_time_holder = red_set.findViewById<TextView>(R.id.red_day)


                    red_holder.text = txHeart.toString()
                    red_time_holder.text = txTime.toString()

                    //end

                    // set up coupon sale tx

                    val coupon_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.coupon_record,null)
                    val coupon_holder = coupon_set.findViewById<TextView>(R.id.coupon_record)
                    val coupon_time_holder = coupon_set.findViewById<TextView>(R.id.coupon_time)

                    coupon_holder.text = txAmount!!.roundToInt().toString()
                    coupon_time_holder.text = txTime.toString()


                    // end

                    // set up coupon redeem tx

                    val coupon_redeem_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.coupon_record,null)
                    val coupon_redeem_holder = coupon_redeem_set.findViewById<TextView>(R.id.coupon_record)
                    val coupon_time_redeem_holder = coupon_redeem_set.findViewById<TextView>(R.id.coupon_time)
                    val coupon_redeem_type = coupon_redeem_set.findViewById<TextView>(R.id.type)
                    val coupon_redeem_unit = coupon_redeem_set.findViewById<TextView>(R.id.unit)

                    coupon_redeem_holder.text = txCoupon.toString()
                    coupon_time_redeem_holder.text = txTime.toString()
                    coupon_redeem_type.text = "coupon redeem: "
                    coupon_redeem_unit.text = "ดวง : "

                    // end

                    if (txType == "sale") {
                        tx_scroll_view.addView(tx_set)
                    }else if (txType =="redeem"){
                        tx_scroll_view.addView(red_set)
                    }else if(txType == "coupon"){
                        tx_scroll_view.addView(coupon_set)
                    }else if(txType == "coupon_redeem"){
                        tx_scroll_view.addView(coupon_redeem_set)
                    }


                    // setup tx_del
                    var txKey = ds.key.toString()


                    val tx_del_btn = tx_set.findViewById<ImageButton>(R.id.tx_del)

                    tx_del_btn.setOnClickListener{

                        if (txKey != null) {
                            tx_delete(txKey,
                                txAmount!!,
                                txTime!!

                            )
                        }

                    }

                    //ends

                    // setup red_del
                    val red_del_btn = red_set.findViewById<ImageButton>(R.id.red_del)

                    red_del_btn.setOnClickListener{

                        if (txKey != null) {
                            tx_delete(txKey,
                                txAmount!!,
                                txTime!!

                            )
                        }
                    }

                    // ends

                    // setup coupon_del
                    val coupon_del = coupon_set.findViewById<ImageButton>(R.id.coupon_del)

                    coupon_del.setOnClickListener{

                        if (txKey != null) {
                            tx_delete(txKey,
                                txAmount!!,
                                txTime!!

                            )
                        }

                    }

                    // ends

                    // setup coupon_redeem_del
                    val coupon_redeem_del = coupon_redeem_set.findViewById<ImageButton>(R.id.coupon_del)

                    coupon_redeem_del.setOnClickListener{

                        if (txKey != null) {
                            tx_delete(txKey,
                                txAmount!!,
                                txTime!!

                            )
                        }

                    }

                    // ends
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

    private fun tx_delete(txKey:String,txAmount:Float,txTime:String){


        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.tx_del_dialog,null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this@biz_dashboard)
            .setView(mDialogView)
            .setTitle("")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        mDialogView.tx_del_amount.text = txAmount.toString()
        mDialogView.tx_del_time.text = txTime.toString()
//        mDialogView.tx_del_month.text = txMonth.toString()
//        mDialogView.tx_del_year.text = txYear.toString()
//        mDialogView.tx_del_hour.text = txHour.toString()
//        mDialogView.tx_del_minute.text = txMinute.toString()

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
                var upsales_sum = 0f

                // reset heart counters
                heart_sum = 0
                heart_used = 0
                for (ds in dataSnapshot.children) {

                    amount_sum += ds.child("amount").getValue(Float::class.java)!!
                    upsales_sum += ds.child("upsale").getValue(Float::class.java)!!

                    if(ds.child("type").getValue(String::class.java)!! == "redeem"){
                        heart_used +=ds.child("heartBank").getValue(Int::class.java)!!
                    }

                    // get heart sum and exp warning

                    val heart_time = ds.child("time").getValue(Long::class.java)!!
                    val time = System.currentTimeMillis() / 1000L

                    val exp_time = heart_time+(heart_life*86400)

                    if(exp_time>time){
                        heart_sum += ds.child("heartBank").getValue(Int::class.java)!!
                    }

                    if(exp_time<heart_exp_warning|| heart_exp_warning == 0L){
                        heart_exp_warning = exp_time
                    }
                }

                customer_total.text = amount_sum.toString()
                customer_heart.text = heart_sum.toString()
                customer_upsale.text = upsales_sum.toString()
                customer_heart_used.text = abs(heart_used).toString()

                val time = System.currentTimeMillis() / 1000L
                val heart_exp = (heart_exp_warning-time)/86400
                customer_heart_countdown_display.text = heart_exp.toString()

                // get promos

                 promos_database_read()


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

    private fun promos_database_read(){

        //read data

        var biz_uid = auth.currentUser!!.uid

        var promos_ref  = database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).orderByChild("promoWorth")

        val promos_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // clean out frame
                biz_promo_frame.removeAllViews()

                for (dsc in dataSnapshot.child("coupons").children) {

                    var promoName = dsc.child("promoName").getValue(String::class.java)
                    var promoWorth = dsc.child("promoWorth").getValue(Int::class.java)
                    var couponLife = dsc.child("couponLife").getValue(Int::class.java)
                    var couponPrice = dsc.child("price").getValue(Float::class.java)
                    var couponKey = dsc.key.toString()



                    val promo_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.biz_promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val couponWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val promoWorth_img_btn = promo_set.findViewById<ImageButton>(R.id.heartButton)
                    promoName_holder.text = promoName+" | "+couponLife+" วัน | "+couponPrice!!.roundToInt().toString()+" บ."
                    promoWorth_img_btn.setEnabled(false)

                    // get couponBalance read database again

                    var coupon_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(customer_logged_phone.toString())

                    val coupon_listener = object : ValueEventListener {


                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            // clear coupon balance

                            coupon_balance = 0


                            for (dscb in dataSnapshot.children) {


                                if(dscb.child("key").getValue(String::class.java).toString() == couponKey) {
                                    val coupon_count =
                                        dscb.child("couponBank").getValue(Int::class.java)!!

                                    // get exp time parameters
                                    val coupon_time =
                                        dscb.child("time").getValue(Long::class.java)!!
                                    val time = System.currentTimeMillis() / 1000L
                                    val exp_time = coupon_time + (couponLife!!* 86400)

                                    if (exp_time > time) {
                                        coupon_balance += coupon_count
                                        val time_left = (exp_time - time)/86400

                                        promoName_holder.text = promoName + " | " + time_left.toString() + " วัน"
                                    }
                                }


                                couponWorth_holder.text = coupon_balance.toString()

                                // check coupon balance and set btn status



                                if(coupon_balance > 0){
                                    promoWorth_img_btn.setBackgroundResource(R.drawable.coupon_btn)
                                    promoWorth_img_btn.setImageResource(R.drawable.g_heart)
                                    promoWorth_img_btn.setEnabled(true)
                                    promoName_holder.setBackgroundResource(R.drawable.coupon_btn)
                                    promoName_holder.setTextColor(getColor(R.color.colorWhite))
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

                    coupon_ref.addValueEventListener(coupon_listener)



                    promoWorth_img_btn.setOnClickListener{

                        //coupon amount seletor

                        //Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.coupon_amount,null)

                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(this@biz_dashboard)
                            .setView(mDialogView)
                            .setTitle("กรุณาเลือกจำนวนหัวใจที่จะใช้")


                        //show dialog
                        val  mAlertDialog = mBuilder.show()

                        // disable touch outside

                        mAlertDialog.setCanceledOnTouchOutside(false)


                        // enable btn if text field is not empty - start

                        mDialogView.coupon_amount.addTextChangedListener(object :
                            TextWatcher {
                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) { if (s.toString().trim { it <= ' ' }.length > 0){
                                mDialogView.newdevice_save.setEnabled(true)
                                mDialogView.newdevice_save.setBackgroundColor(resources.getColor(R.color.colorBackground))

                            }else{
                                mDialogView.newdevice_save.setEnabled(false)
                                mDialogView.newdevice_save.setBackgroundColor(resources.getColor(R.color.colorDisabled))

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
                        mDialogView.newdevice_save.setOnClickListener {

                            // send data to biz_redeem

                            val intent = Intent(this@biz_dashboard,biz_redeem::class.java)
                            intent.putExtra("biz_redeem_name",promoName)
                            intent.putExtra("biz_promoWorth",mDialogView.coupon_amount.text.toString())
                            intent.putExtra("biz_coupon_key",couponKey)
                            startActivity(intent)



                            //dismiss dialog
                            mAlertDialog.dismiss()


                        }
                        //cancel button click of custom layout
                        mDialogView.newdevice_cancel.setOnClickListener {
                            //dismiss dialog
                            mAlertDialog.dismiss()
                        }

                        // dialog with editText ends




                        // coupon amount selector ends





                    }


                    biz_promo_frame.addView(promo_set)



                }


                // add separator
                val line_view = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.line,null)
                biz_promo_frame.addView(line_view)


                for (ds in dataSnapshot.child("promos").children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)
                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java)



                    val promo_set = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.biz_promo_set,null)
                    val promoName_holder = promo_set.findViewById<TextView>(R.id.promoName_textview)
                    val promoWorth_holder = promo_set.findViewById<TextView>(R.id.promoWorth_textview)
                    val promoWorth_img_btn = promo_set.findViewById<ImageButton>(R.id.heartButton)
                    promoName_holder.text = promoName
                    promoWorth_holder.text = promoWorth.toString()

                    // check heart bank and set btn status

                    promoWorth_img_btn.setEnabled(false)

                    if(heart_sum - promoWorth!! >= 0){
//                        val active_color = ContextCompat.getColor(this@customer,R.color.colorHeart)
                        promoWorth_img_btn.setBackgroundResource(R.drawable.heart_btn)
                        promoWorth_img_btn.setEnabled(true)
                        promoName_holder.setBackgroundResource(R.drawable.enabled_border)
                    }

                    promoWorth_img_btn.setOnClickListener{

                        val intent = Intent(this@biz_dashboard,biz_redeem::class.java)
                        intent.putExtra("biz_redeem_name",promoName)
                        intent.putExtra("biz_promoWorth",promoWorth.toString())
                        startActivity(intent)


                    }


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

//    private fun couponBalance(couponKey: String,couponLife:Int){
//
//
//
//
//        //read data
//
//        var tx_ref  = database.child("transactions").child(global_location_key.toString()).orderByChild("phone").equalTo(customer_logged_phone.toString())
//
//        val tx_listener = object : ValueEventListener {
//
//
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//                // clear coupon balance
//
//                coupon_balance = 0
//
//
//                for (ds in dataSnapshot.children) {
//
//
//                    if(ds.child("key").getValue(String::class.java).toString() == couponKey){
//
////                        Log.d("test","matched"+ds.child("couponBank").getValue(Int::class.java).toString())
//
//                        val coupon_count = ds.child("couponBank").getValue(Int::class.java)
//                        if(coupon_count != null){
//                            coupon_balance += coupon_count}
//                    }
//
//                    couponWorth_holder.text





                    // get heart sum and exp warning
//
//                            val heart_time = ds.child("time").getValue(Long::class.java)!!
//                            val time = System.currentTimeMillis() / 1000L
//
//                            val exp_time = heart_time+(heart_life*86400)
//
//                            if(exp_time>time){
//                                heart_sum += ds.child("heartBank").getValue(Int::class.java)!!
//                            }
//
//                            if(exp_time<heart_exp_warning|| heart_exp_warning == 0L){
//                                heart_exp_warning = exp_time
//                            }
//                }
//
//
//
//
//
//
//
//
//
//            }
//
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w("error",databaseError.toException())
//                // [START_EXCLUDE]
//                Toast.makeText(baseContext, "database failed",
//                    Toast.LENGTH_SHORT).show()
//                // [END_EXCLUDE]
//            }
//
//
//        }
//
//        tx_ref.addValueEventListener(tx_listener)
//
//
//        // database ends
////TODO: get return value back to display
////        return coupon_balance.toString()
//
//
//
//
//
//
//    }


    private fun coupon_selector(){


        // read database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var select_ref  =  database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("coupons").orderByChild("promoWorth")

        val select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var radioID = 0

                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this@biz_dashboard).inflate(R.layout.dialog_frame,null)

                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this@biz_dashboard)
                    .setView(mDialogView)
                    .setTitle("เลือกซื้อ coupon")
                    .setCancelable(false)
                //show dialog
                val  mAlertDialog = mBuilder.show()

                // disable touch outside

                mAlertDialog.setCanceledOnTouchOutside(false)

                mDialogView.frame_next.text = "บันทึก"


                for (ds in dataSnapshot.children) {

                    var promoName = ds.child("promoName").getValue(String::class.java)
                    var promoKey = ds.key
                    var promoWorth = ds.child("promoWorth").getValue(Int::class.java).toString()
                    var couponLife = ds.child("couponLife").getValue(Int::class.java).toString()
                    var couponPrice = ds.child("price").getValue(Float::class.java)!!.roundToInt().toString()

                    val radioButton = RadioButton(this@biz_dashboard)
                    radioID += 1

                    radioButton.layoutParams= LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                    radioButton.setText(promoName+" จำนวน "+promoWorth+" หัวใจ หมดอายุใน "+couponLife+" วัน ราคา "+couponPrice+" บาท")
                    radioButton.id = radioID
                    radioButton.tag = promoKey

                   if (radioID == 1){

                       radioButton.isChecked = true

                   }




                    mDialogView.radioSet_group.addView(radioButton)


                }

                //next btn
                mDialogView.frame_next.setOnClickListener {

                    // get id from radio group

                    var id: Int = mDialogView.radioSet_group.checkedRadioButtonId



                    if(id!=-1){
                        //set location key
                        val radio:RadioButton =mDialogView.radioSet_group.findViewById(id)

                        //dismiss dialog
                        mAlertDialog.dismiss()

                        // call confirm delete fun

                        coupon_tx_record(radio.tag.toString())



                    }else{

                        // no radio selected

                        toast("กดเลือก coupon ด้วยครับ")
                    }

                }

                //cancel button click of custom layout
                mDialogView.frame_cancel.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }


                // dialog with editText ends


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
        select_ref.addListenerForSingleValueEvent(select_listener)
        // read location database ends





    }

    private fun coupon_tx_record(couponKey:String){


        // read database then add radio set to dialog

        var biz_uid = auth.currentUser!!.uid

        var select_ref  =  database.child("biz_owners").child(biz_uid).child(global_location_key.toString()).child("coupons").child(couponKey)

        val select_listener = object : ValueEventListener {


            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var couponWorth = 0
                var couponPrice = 0.0F

                for (ds in dataSnapshot.children) {


                    when (ds.key){
                       "promoWorth" -> couponWorth = ds.getValue(Int::class.java)!!
                       "price"->  couponPrice = ds.getValue(Float::class.java)!!
                    }
                }
                // tx write to database

                val transaction_insert = Coupons_tx(customer_logged_phone,0,couponWorth,couponPrice,couponPrice,"coupon",couponKey)

                database.child("transactions").child(global_location_key.toString()).push().setValue(transaction_insert)


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
        select_ref.addListenerForSingleValueEvent(select_listener)
        // read location database ends





    }


    fun toast(msg:String){

        Toast.makeText(
            baseContext, msg,
            Toast.LENGTH_LONG
        ).show()


    }

}
