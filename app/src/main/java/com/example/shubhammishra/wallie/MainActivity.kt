package com.example.shubhammishra.wallie

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main)
        blurImageView.setBlur(2)
        progress.visibility= View.VISIBLE
        Handler().postDelayed(object:Runnable{
            override fun run() {
                progress.visibility=View.GONE
                val intent= Intent(this@MainActivity,Wallpaper::class.java)
                startActivity(intent)
                finish()
            }
        },4000)
    }
}
