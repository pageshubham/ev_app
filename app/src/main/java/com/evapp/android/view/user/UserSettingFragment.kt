package com.evapp.android.view.user

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.evapp.android.R
import com.evapp.android.view.auth.SelectionActivity
import com.evapp.android.view.auth.UserLoginActivity
import com.evapp.android.view.vendor.VendorProfileFragment
import com.google.firebase.auth.FirebaseAuth

class UserSettingFragment : Fragment() {

    private var TAG = "UserSettingFragment"
    private lateinit var auth: FirebaseAuth

    lateinit var user_setting_contribute_layout: RelativeLayout
    lateinit var user_setting_profile_layout: RelativeLayout
    lateinit var user_setting_vehicle_layout: RelativeLayout
    lateinit var user_setting_about_layout: RelativeLayout
    lateinit var user_setting_logout_layout: TextView

    lateinit var logout_dialog: Dialog
    private lateinit var single_user_logout_yes_button: Button
    private lateinit var single_user_logout_no_button: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_setting, container, false)

        auth = FirebaseAuth.getInstance()

        user_setting_contribute_layout = view.findViewById(R.id.user_setting_contribute_layout)
        user_setting_profile_layout = view.findViewById(R.id.user_setting_profile_layout)
        user_setting_vehicle_layout = view.findViewById(R.id.user_setting_vehicle_layout)
        user_setting_about_layout = view.findViewById(R.id.user_setting_about_layout)
        user_setting_logout_layout = view.findViewById(R.id.user_setting_logout_layout)

        //logout_dialog
        logout_dialog = Dialog(requireContext())
        logout_dialog.setContentView(R.layout.single_user_logout_dialog)
        single_user_logout_yes_button = logout_dialog.findViewById(R.id.single_user_logout_yes_button)
        single_user_logout_no_button = logout_dialog.findViewById(R.id.single_user_logout_no_button)
        logout_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logout_dialog.setCancelable(true)

        user_setting_contribute_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, UserContributeFragment(), "user_contribute")
            transaction.addToBackStack("user_contribute")
            transaction.commit()
        }

        user_setting_profile_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, UserProfileFragment(), "user_profile")
            transaction.addToBackStack("user_profile")
            transaction.commit()
        }

        user_setting_vehicle_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, UserVehicleFragment(), "user_vehicle")
            transaction.addToBackStack("user_vehicle")
            transaction.commit()
        }

        user_setting_about_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, UserAboutFragment(), "user_about")
            transaction.addToBackStack("user_about")
            transaction.commit()
        }

        val content = SpannableString("Logout")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        user_setting_logout_layout.text = content

        user_setting_logout_layout.setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    private fun showLogoutDialog() {
        logout_dialog.show()
        val window = logout_dialog.window
        window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        single_user_logout_yes_button.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, SelectionActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            logout_dialog.dismiss()
        }

        single_user_logout_no_button.setOnClickListener {
            logout_dialog.dismiss()
        }
    }
}