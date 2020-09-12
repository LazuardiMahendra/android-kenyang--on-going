package com.example.kenyangproject.Navigatinon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kenyangproject.Model.ContentMain
import com.example.kenyangproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        rv_comment.adapter = CommentAdapter()
        rv_comment.layoutManager = LinearLayoutManager(this)

        bt_comment_send?.setOnClickListener {
            var comment = ContentMain.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = et_comment_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comment").document().set(comment)
            et_comment_message.setText("")
        }
    }

    inner class CommentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<ContentMain.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comment")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null)
                        return@addSnapshotListener
                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(ContentMain.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView
            view.tv_comment_message.text = comments[p1].comment
            view.tv_comment_profile_id.text = comments[p1].userId


            FirebaseFirestore.getInstance()
                .collection("profileImage")
                .document(comments[p1].uid!!)
                .get()
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.iv_comment_profile)
                    }

                }
        }

    }
}
