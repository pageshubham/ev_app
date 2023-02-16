package com.evapp.android.view.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evapp.android.R

class UserAboutFragment : Fragment() {

    lateinit var user_about_back_icon: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_about, container, false)

        user_about_back_icon = view.findViewById(R.id.user_about_back_icon)

        user_about_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_about")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_about", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        return view
    }
}