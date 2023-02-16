package com.evapp.android.view.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.evapp.android.R
import com.evapp.android.view.main.UserMainActivity
import com.evapp.android.view.main.VendorMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_selection.*

class SelectionActivity : AppCompatActivity() {

    private var TAG = "SelectionActivity"
    private var VENDOR = "VENDOR"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        auth = FirebaseAuth.getInstance()

        selection_user_button.setOnClickListener {
            val intent = Intent(this@SelectionActivity, UserLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        selection_vendor_button.setOnClickListener {
            val intent = Intent(this@SelectionActivity, VendorLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }//onCreate()

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.uid
        if (currentUser != null) {
            PreferenceManager.getDefaultSharedPreferences(this).apply {
                if (getBoolean(VENDOR, true)) {
                    val intent = Intent(this@SelectionActivity, VendorMainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@SelectionActivity, UserMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}