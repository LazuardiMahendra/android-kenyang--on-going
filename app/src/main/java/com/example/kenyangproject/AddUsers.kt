package com.example.kenyangproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.add_user.*

class AddUsers : AppCompatActivity() {

    lateinit var ref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_user)

        ref = FirebaseDatabase.getInstance().getReference("USERS")

        bt_save_user.setOnClickListener {
            savedata()
        }
    }

    private fun savedata() {
        val username = add_username.text.toString()
        val email = add_email.text.toString()
        val telp = add_telp.text.toString()

        val userId = ref.push().key.toString()
        val user =
            Users(userId, username, email, telp)


        ref.child(userId).setValue(user).addOnCompleteListener {
            Toast.makeText(this, "Successs", Toast.LENGTH_SHORT).show()
            add_username.setText("")
            add_telp.setText("")
            add_email.setText("")
        }
    }
}
