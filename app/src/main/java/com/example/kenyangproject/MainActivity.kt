package com.example.kenyangproject

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kenyangproject.Navigatinon.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        bn_main.setOnNavigationItemSelectedListener(this)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        //set default screen
        bn_main.selectedItemId = R.id.home_menu


    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        setToolbarDefault()
        when (p0.itemId) {
            R.id.home_menu -> {
                var homeViewFragment = HomeViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_main, homeViewFragment).commit()
                return true
            }
            R.id.resep_menu -> {
                var resepViewFragment = ResepViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_main, resepViewFragment).commit()
                return true
            }
            R.id.addresep_menu -> {
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this,AddResep::class.java))
                }
                return true
            }
            R.id.notifikasi_menu-> {
                var notifikasiViewFragment = NotifikasiViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fl_main, notifikasiViewFragment).commit()
                return true
            }
            R.id.profil_menu -> {
                var profilViewFragment = ProfilViewFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                profilViewFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.fl_main, profilViewFragment).commit()
                return true
            }
        }
        return false
    }

    fun setToolbarDefault(){
        tb_bt_back.visibility =View.GONE
        tb_main_username.visibility =View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ProfilViewFragment.PICKER_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImage").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask{task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String, Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImage").document(uid).set(map)
            }
        }
    }
 }


