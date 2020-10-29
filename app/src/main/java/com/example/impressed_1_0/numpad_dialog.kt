package com.example.impressed_1_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_numpad_dialog.*

class numpad_dialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_numpad_dialog)

        // disable softkeyboard on focus

        otp.setShowSoftInputOnFocus(false)

        otp.hint = "test"






    }
}
