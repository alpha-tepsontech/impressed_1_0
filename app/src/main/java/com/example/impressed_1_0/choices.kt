package com.example.impressed_1_0

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_choices.*
import kotlinx.android.synthetic.main.activity_dashboard.*


private var tier_1_price:String = ""
private var tier_2_price:String = ""
private var tier_3_price:String = ""
private var device_count:Int = 0




class choices : AppCompatActivity() {


    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    // firebase realtime database setup
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choices)

        // lock orientation while on this activity
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // initialize_database_ref
        database = Firebase.database.reference

        pricing_read()



    }

    private fun pricing_read(){


        // get data from database

        var biz_uid = auth.currentUser!!.uid

        var pricing_ref  = database.child("biz_owners").child(biz_uid)

        val pricing_listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // count devices

                for (ds in dataSnapshot.child("devices").children){
                    device_count += 1

                }



                // tier_1 text setup
                for (ds in dataSnapshot.child("pricing").child("tier_1").children) {
                    when(ds.key){
                        "name" -> tier_1_name.text = ds.getValue(String::class.java)
                        "monthly" -> monthly_1.text = ds.getValue(String::class.java)
                        "total" -> {
                            val tier_1_raw = device_count*ds.getValue(String::class.java)!!.toInt()
                            tier_1_price = moneyFormat(tier_1_raw.toString())
                            tier_1_total.text = tier_1_price
                            tier_1_count.text = device_count.toString()


                        }
                    }
                }

                // tier_2 text setup
                for (ds in dataSnapshot.child("pricing").child("tier_2").children) {
                    when(ds.key){
                        "name" -> tier_2_name.text = ds.getValue(String::class.java)
                        "monthly" -> monthly_2.text = ds.getValue(String::class.java)
                        "total" -> {
                            val tier_2_raw = device_count*ds.getValue(String::class.java)!!.toInt()
                            tier_2_price = moneyFormat(tier_2_raw.toString())
                            tier_2_total.text = tier_2_price
                            tier_2_count.text = device_count.toString()
                        }
                    }
                }

                // tier_3 text setup
                for (ds in dataSnapshot.child("pricing").child("tier_3").children) {
                    when(ds.key){
                        "name" -> tier_3_name.text = ds.getValue(String::class.java)
                        "monthly" -> monthly_3.text = ds.getValue(String::class.java)
                        "total" -> {
                            val tier_3_raw = device_count*ds.getValue(String::class.java)!!.toInt()
                            tier_3_price = moneyFormat(tier_3_raw.toString())
                            tier_3_total.text = tier_3_price
                            tier_3_count.text = device_count.toString()
                        }
                    }
                }



            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("error",  databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Failed to load data.",
                    Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }


        pricing_ref.addListenerForSingleValueEvent(pricing_listener)

        // database ends


        set_next_btn()

    }


    private fun set_next_btn(){

        tier_1_nextBtn.setOnClickListener{
            val intent = Intent(this@choices,payment::class.java)
            intent.putExtra("total", tier_1_price)
            startActivity(intent)
        }

        tier_2_nextBtn.setOnClickListener{
            val intent = Intent(this@choices,payment::class.java)
            intent.putExtra("total", tier_2_price)
            startActivity(intent)
        }

        tier_3_nextBtn.setOnClickListener{
            val intent = Intent(this@choices,payment::class.java)
            intent.putExtra("total", tier_3_price)
            startActivity(intent)
        }

    }


}