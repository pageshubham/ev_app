package com.evapp.android.view.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.evapp.android.R
import com.evapp.android.view.main.UserMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user_login.*
import java.util.regex.Pattern

class UserLoginActivity : AppCompatActivity() {

    private var TAG = "UserLoginActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var VENDOR = "VENDOR"
    private var PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z0-9@#$%&])(?=\\S+$).{7,}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        user_login_back_icon.setOnClickListener {
            val intent = Intent(this@UserLoginActivity, SelectionActivity::class.java)
            startActivity(intent)
            finish()
        }

        user_login_authenticate_button.setOnClickListener {
            confirmDetails()
        }

        user_login_new_user.setOnClickListener {
            val intent = Intent(this@UserLoginActivity, UserSignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        val text = "New user? click here"
        val string = SpannableString(text)
        val fcsPrimary = ForegroundColorSpan(Color.parseColor("#0E17FF"))
        string.setSpan(fcsPrimary, 9, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        user_login_new_user.text = string

    }//onCreate()

    private fun validateEmail(): Boolean {
        val emailInput = user_login_email_input.text.toString().trim()
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

    private fun validatePassword(): Boolean {
        val passwordInput = user_login_password_input.text.toString().trim()
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

    private fun confirmDetails() {
        if (!validateEmail() or !validatePassword()) {
            return
        } else {
            user_login_new_user.visibility = View.GONE
            user_login_progress_bar.visibility = View.VISIBLE

            val emailInput = user_login_email_input.text.toString().trim()
            val passwordInput = user_login_password_input.text.toString().trim()
            signInWithEmailAndPassword(emailInput, passwordInput)
        }
    }

    private fun signInWithEmailAndPassword(emailInput: String, passwordInput: String) {
        auth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    PreferenceManager.getDefaultSharedPreferences(this@UserLoginActivity).edit().apply {
                        putBoolean(VENDOR, false)
                        apply()

                        val intent = Intent(this@UserLoginActivity, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@UserLoginActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    user_login_new_user.visibility = View.VISIBLE
                    user_login_progress_bar.visibility = View.GONE
                }
            }
    }

}