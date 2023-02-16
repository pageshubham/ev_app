package com.evapp.android.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.evapp.android.R
import com.evapp.android.view.main.VendorMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_vendor_signup.*
import java.util.regex.Pattern

class VendorSignupActivity : AppCompatActivity() {

    private var TAG = "VendorSignupActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z0-9@#$%&])(?=\\S+$).{7,}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        vendor_signup_back_icon.setOnClickListener {
            finish()
        }

        vendor_signup_register_button.setOnClickListener {
            confirmDetails()
        }

    }//onCreate()

    private fun validateName(): Boolean {
        val nameInput = vendor_signup_name_input.text.toString().trim()
        return if (nameInput.isEmpty()) {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateEmail(): Boolean {
        val emailInput = vendor_signup_email_input.text.toString().trim()
        return when {
            emailInput.isEmpty() -> {
                Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() -> {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                true
            }
        }
    }

    private fun validateGST(): Boolean {
        val gstInput = vendor_signup_gst_input.text.toString().trim()
        return if (gstInput.isEmpty()) {
            Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validatePassword(): Boolean {
        val passwordInput = vendor_signup_password_input.text.toString().trim()
        return when {
            passwordInput.isEmpty() -> {
                Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !PASSWORD_PATTERN.matcher(passwordInput).matches() -> {
                Toast.makeText(this, "Password pattern mismatch", Toast.LENGTH_SHORT).show()
                false
            }
            passwordInput.length < 6 -> {
                Toast.makeText(this, "Password must be greater than 6", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val passwordInput = vendor_signup_password_input.text.toString().trim()
        val confirmPasswordInput = vendor_signup_confirm_password_input.text.toString().trim()
        return when {
            confirmPasswordInput.isEmpty() -> {
                Toast.makeText(this, "Field can't be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !PASSWORD_PATTERN.matcher(confirmPasswordInput).matches() -> {
                Toast.makeText(this, "Password pattern mismatch", Toast.LENGTH_SHORT).show()
                false
            }
            confirmPasswordInput.length < 6 -> {
                Toast.makeText(this, "Password must be greater than 6", Toast.LENGTH_SHORT).show()
                false
            }
            confirmPasswordInput != passwordInput -> {
                Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                true
            }
        }
    }

    private fun confirmDetails() {
        if (!validateName() or !validateEmail() or !validateGST() or
            !validatePassword() or !validateConfirmPassword()) {
            return
        } else {
            vendor_signup_progress_bar.visibility = View.VISIBLE
            val emailInput = vendor_signup_email_input.text.toString().trim()
            val passwordInput = vendor_signup_password_input.text.toString().trim()
            createUserWithEmailAndPassword(emailInput, passwordInput)
        }
    }

    private fun createUserWithEmailAndPassword(emailInput: String, passwordInput: String) {
        auth.createUserWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    createAccount(auth.uid!!)
                } else {
                    Toast.makeText(this@VendorSignupActivity, "Signup Failed", Toast.LENGTH_SHORT).show()
                    vendor_signup_progress_bar.visibility = View.GONE
                }
            }
    }

    private fun createAccount(uid: String) {
        val nameInput = vendor_signup_name_input.text.toString().trim()
        val emailInput = vendor_signup_email_input.text.toString().trim()
        val gstInput = vendor_signup_gst_input.text.toString().trim()
        val passwordInput = vendor_signup_password_input.text.toString().trim()

        val ref = database.getReference("vendor").child("account").child(uid)
        ref.child("uid").setValue(uid)
        ref.child("name").setValue(nameInput)
        ref.child("email").setValue(emailInput)
        ref.child("gst_no").setValue(gstInput)
        ref.child("phone").setValue("")
        ref.child("password").setValue(passwordInput)
        ref.child("area").setValue("")
        ref.child("city").setValue("")
        ref.child("state").setValue("")
        ref.child("country").setValue("")
        ref.child("latitude").setValue(0.0)
        ref.child("longitude").setValue(0.0)
        ref.child("postal").setValue("")
            .addOnSuccessListener {
                val intent = Intent(this@VendorSignupActivity, VendorMainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this@VendorSignupActivity, it.message, Toast.LENGTH_LONG).show()
                vendor_signup_progress_bar.visibility = View.GONE
            }
    }

}