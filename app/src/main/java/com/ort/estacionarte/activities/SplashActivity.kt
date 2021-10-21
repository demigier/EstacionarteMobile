package com.ort.estacionarte.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.ort.estacionarte.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val sharedPref: SharedPreferences = applicationContext.getSharedPreferences("Session", Context.MODE_PRIVATE)
        var userID = sharedPref.getString("userID","default")
        Log.d("test", userID!!)
        Handler().postDelayed({

            if(userID != "default"){
                Log.d("TestRedirect", userID)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }else{
                Log.d("TestRedirect2", userID)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 1000)
    }
}