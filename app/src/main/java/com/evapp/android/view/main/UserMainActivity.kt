package com.evapp.android.view.main

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.evapp.android.R
import com.evapp.android.view.user.UserBookingFragment
import com.evapp.android.view.user.UserHomeFragment
import com.evapp.android.view.user.UserSettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserMainActivity : AppCompatActivity() {

    private var addedFragment: ArrayList<String> = ArrayList()
    private lateinit var bottom_navigation_bar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        bottom_navigation_bar = findViewById(R.id.bottom_navigation_bar)
        bottom_navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        addedFragment.add("home")

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment, UserHomeFragment(), "user_home")
        transaction.commit()

    }//onCreate()

    private fun replaceFragment(activity: FragmentActivity, fragment: Fragment, tag:String, addedFragment: ArrayList<String>) {
        try {
            if (fragment != null) {
                val fragmentManager = activity.supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                val currentFragment = fragmentManager.findFragmentByTag(tag)
                for (frag in addedFragment) {
                    val tempFrag = fragmentManager.findFragmentByTag(frag)
                    if (tempFrag != null)
                        transaction.hide(tempFrag)
                }
                if (currentFragment == null) {
                    transaction.add(R.id.fragment, fragment, tag)
                    transaction.addToBackStack(null)
                } else {
                    transaction.show(currentFragment)
                }
                transaction.commit()
            }
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.navigation_home -> {
                if (!addedFragment.contains("user_home"))
                    addedFragment.add("user_home")
                replaceFragment(this@UserMainActivity, UserHomeFragment(), "user_home", addedFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_charge -> {
                if (!addedFragment.contains("user_booking"))
                    addedFragment.add("user_booking")
                replaceFragment(this@UserMainActivity, UserBookingFragment(), "user_booking", addedFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                if (!addedFragment.contains("user_setting"))
                    addedFragment.add("user_setting")
                replaceFragment(this@UserMainActivity, UserSettingFragment(), "user_setting", addedFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

}