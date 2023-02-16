package com.evapp.android.view.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.evapp.android.R

class SplashActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val sDelay: Long = 1000

    private val mRunnable: Runnable = Runnable {

        if (!isFinishing) {
            val intent = Intent(this@SplashActivity, SelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, sDelay)

    }
    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }
}