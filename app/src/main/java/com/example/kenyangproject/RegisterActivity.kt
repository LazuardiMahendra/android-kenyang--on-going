package com.example.kenyangproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.register_activity.*



class RegisterActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        auth = FirebaseAuth.getInstance()

        bt_save.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
//            val username = inputUsername.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth
                    val intent = Intent (this, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Succesfully New Account Create", Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()

                }
            }
                // ...
            }


    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth
    }
}
