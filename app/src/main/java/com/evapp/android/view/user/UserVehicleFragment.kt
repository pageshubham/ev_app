package com.evapp.android.view.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.evapp.android.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserVehicleFragment : Fragment() {

    private var TAG = "UserVehicleFragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var uid = ""

    lateinit var user_vehicle_back_icon: LinearLayout
    lateinit var user_vehicle_name_spinner: AppCompatSpinner
    lateinit var user_vehicle_model_spinner: AppCompatSpinner
    lateinit var user_vehicle_save_button: Button

    private var vehicleCompany = ""
    private var vehicleModel = ""
    private var mVehicleCompany: MutableList<String> = ArrayList()
    private var mVehicleModel: MutableList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_vehicle, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        user_vehicle_back_icon = view.findViewById(R.id.user_vehicle_back_icon)
        user_vehicle_name_spinner = view.findViewById(R.id.user_vehicle_name_spinner)
        user_vehicle_model_spinner = view.findViewById(R.id.user_vehicle_model_spinner)
        user_vehicle_save_button = view.findViewById(R.id.user_vehicle_save_button)

        user_vehicle_back_icon.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_vehicle")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_vehicle", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        mVehicleCompany = resources.getStringArray(R.array.vehicle_company).toMutableList()
        val adapterCompany: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mVehicleCompany)
        adapterCompany.setDropDownViewResource(R.layout.single_spinner_item_layout)
        user_vehicle_name_spinner.adapter = adapterCompany

        user_vehicle_name_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        vehicleCompany = user_vehicle_name_spinner.selectedItem.toString()
                    }
                }
            }

        mVehicleModel = resources.getStringArray(R.array.vehicle_model).toMutableList()
        val adapterModel: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.single_spinner_item_layout, mVehicleModel)
        adapterModel.setDropDownViewResource(R.layout.single_spinner_item_layout)
        user_vehicle_model_spinner.adapter = adapterModel

        user_vehicle_model_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        vehicleModel = user_vehicle_model_spinner.selectedItem.toString()
                    }
                }
            }

        user_vehicle_save_button.setOnClickListener {
            Toast.makeText(requireContext(), "Vehicle Updated", Toast.LENGTH_SHORT).show()

            val fragmentManager = requireActivity().supportFragmentManager
            val frag = fragmentManager.findFragmentByTag("user_vehicle")!!
            val transaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack("user_vehicle", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            transaction.remove(frag).commit()
        }

        return view

    }//onCreate()
}