package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class payment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val qr_image = findViewById(R.id.qr_image) as ImageView

        Picasso.get()
            .load("https://raw.githubusercontent.com/bumptech/glide/master/static/glide_logo.png")
            .resize(200,200)
            .into(qr_image)



    }
}