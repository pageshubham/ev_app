package com.evapp.android.view.vendor

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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.evapp.android.R
import com.evapp.android.view.auth.SelectionActivity
import com.evapp.android.view.auth.VendorLoginActivity
import com.google.firebase.auth.FirebaseAuth

class VendorSettingFragment : Fragment() {

    private var TAG = "VendorSettingFragment"
    private lateinit var auth: FirebaseAuth

    lateinit var vendor_setting_profile_layout: RelativeLayout
    lateinit var vendor_setting_about_layout: RelativeLayout
    lateinit var vendor_setting_logout_layout: TextView

    lateinit var logout_dialog: Dialog
    private lateinit var single_vendor_logout_yes_button: Button
    private lateinit var single_vendor_logout_no_button: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_vendor_setting, container, false)

        auth = FirebaseAuth.getInstance()

        vendor_setting_profile_layout = view.findViewById(R.id.vendor_setting_profile_layout)
        vendor_setting_about_layout = view.findViewById(R.id.vendor_setting_about_layout)
        vendor_setting_logout_layout = view.findViewById(R.id.vendor_setting_logout_layout)

        //logout_dialog
        logout_dialog = Dialog(requireContext())
        logout_dialog.setContentView(R.layout.single_vendor_logout_dialog)
        single_vendor_logout_yes_button = logout_dialog.findViewById(R.id.single_vendor_logout_yes_button)
        single_vendor_logout_no_button = logout_dialog.findViewById(R.id.single_vendor_logout_no_button)
        logout_dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logout_dialog.setCancelable(true)

        vendor_setting_profile_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, VendorProfileFragment(), "vendor_profile")
            transaction.addToBackStack("vendor_profile")
            transaction.commit()
        }

        vendor_setting_about_layout.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragment, VendorAboutFragment(), "vendor_about")
            transaction.addToBackStack("vendor_about")
            transaction.commit()
        }

        val content = SpannableString("Logout")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        vendor_setting_logout_layout.text = content

        vendor_setting_logout_layout.setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    private fun showLogoutDialog() {
        logout_dialog.show()
        val window = logout_dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        single_vendor_logout_yes_button.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, SelectionActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            logout_dialog.dismiss()
        }

        single_vendor_logout_no_button.setOnClickListener {
            logout_dialog.dismiss()
        }
    }

}