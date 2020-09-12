package com.example.kenyangproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kenyangproject.Model.ContentMain
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.add_resep.*
import java.text.SimpleDateFormat
import java.util.*


class AddResep : AppCompatActivity()  {

    lateinit var ref : DatabaseReference

    val PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri :Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_resep)

        //initiate to storage,firestore,auth
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()



        //add image upload event
        bt_save_resep.setOnClickListener{
            contentUpload()
        }

        ib_addFotoResep.setOnClickListener{
            openAlbum()
        }

        bt_addBahan.setOnClickListener {

        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //this is patch to the selected image
                photoUri = data?.data
                iv_addFotoResep.setImageURI(photoUri)
            }else{
                //exit the without choose teh photo
                finish()
            }
        }
    }

    fun openAlbum(){
        //open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
    }

    fun contentUpload(){

        //make file name
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask{task : Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->

            var resep = ContentMain()

            //insert downloadurl of images
            resep.resepImage = uri.toString()

            //insert uid of user
            resep.uid = auth?.currentUser?.uid

            //insert nama resep
            resep.resepNama = et_addNamaResep.text.toString()

            //insert langkah resep
            resep.resepLangkah = et_addLangkah.text.toString()

            //insert bahan resep
            resep.resepBahan = et_addBahan.text.toString()

            //insert alat resep
            resep.resepAlat = et_addAlat.text.toString()

            //insert timestamp
            resep.timestamp = System.currentTimeMillis()

            //insert user id
            resep.userId = auth?.currentUser?.email

            firestore?.collection("images")?.document()?.set(resep)
            setResult(Activity.RESULT_OK)



            finish()
            Toast.makeText(this, "Succesfully Recipe Create", Toast.LENGTH_SHORT).show()
        }



    }

}