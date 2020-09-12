package com.example.kenyangproject

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.login_activity.*
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        auth = FirebaseAuth.getInstance()

        bt_sign_in.setOnClickListener { sign_in() }
        tv_sign_up.setOnClickListener { sign_up() }
        bt_sign_in_google.setOnClickListener { googleLogin() }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //.requestIdToken(getString(R.string.default_web_client_id)
            .requestIdToken("744111357037-rob7pq8jlnb542d8urp888jf0kq7256p.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

///function sign in

    fun sign_in() {
        var email = et_email.text.toString()
        var password = et_password.text.toString()
        auth = FirebaseAuth.getInstance()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please Insert Email and Password", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else
                    Toast.makeText(this, "Succesfully Login", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Main", "Failed Login: ${it.message}")
                Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()

            }

    }

    ///function sign up

    fun sign_up() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    ///function sign in Google

    fun googleLogin(){
        var signIntent = googleSignInClient?.signInIntent
        startActivityForResult(signIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == GOOGLE_LOGIN_CODE){
//            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
//            if(result.isSuccess){
//                var account = result.signInAccount
//                firebaseAuthWithGoogle(account)
//            }
//        }
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess){
                    var account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                task ->
                if(task.isSuccessful){
                    //login
                    moveMainPage(task.result?.user)
                }else{
                    //show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }


//    fun printHashKey() {
//        try {
//            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                val hashKey = String(Base64.encode(md.digest(), 0))
//                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
//            }
//        } catch (e: NoSuchAlgorithmException) {
//            Log.e("TAG", "printHashKey()", e)
//        } catch (e: Exception) {
//            Log.e("TAG", "printHashKey()", e)
//        }
//
//    }



}