package com.example.kenyangproject.Navigatinon

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kenyangproject.Model.ContentMain
import com.example.kenyangproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.view_list_detail.view.*
import java.util.ArrayList

class HomeViewFragment : Fragment(){

    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_home,container,false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.rv_detailviewitem.adapter = DetailViewAdapter()
        view.rv_detailviewitem.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class  DetailViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentMains : ArrayList<ContentMain> = arrayListOf()
        var contentMainUidList : ArrayList<String> = arrayListOf()

        init{

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                contentMains.clear()
                contentMainUidList.clear()
                //somtimes, this code return null of querysnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentMain::class.java)
                    contentMains.add(item!!)
                    contentMainUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.view_list_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return  contentMains.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //Photo User
            FirebaseFirestore.getInstance()
                .collection( "profileImage")
                .document(contentMains[p1].uid!!)
                .get()
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(viewholder.iv_detailviewitem_profile)
                    }

                }


            //bahan
            viewholder.tv_detailviewitem_isi_bahan.text = contentMains!![p1].resepBahan
            //alat
            viewholder.tv_detailviewitem_isi_alat.text = contentMains!![p1].resepAlat
            //langkah
            viewholder.tv_detailviewitem_isi_langkah.text = contentMains!![p1].resepLangkah

            //UserId
            viewholder.tv_detailviewitem_profile.text = contentMains!![p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentMains!![p1].resepImage).into(viewholder.iv_detailviewitem_content)

            //name content
            viewholder.tv_detailviewitem_namecontent.text = contentMains!![p1].resepNama

            //like
            viewholder.tv_detailviewitem_favoritecouter.text = "Likes " + contentMains!![p1].favoriteCount

            //this code is when the page loaded
            viewholder.iv_detailviewitem_favorite.setOnClickListener{
                favoriteEvent(p1)
            }
            if (contentMains!![p1].favorites.containsKey(uid)){
                //this is like status
                viewholder.iv_detailviewitem_favorite.setImageResource(R.drawable.ic_favorite)
            }else{
                //this is if unlike
                viewholder.iv_detailviewitem_favorite.setImageResource((R.drawable.ic_favorite_border))
            }

            //toolbar
            //this code is when the profile image in frame is clicked
            viewholder.iv_detailviewitem_profile.setOnClickListener {
                var fragment = ProfilViewFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentMains[p1].uid)
                bundle.putString("userId",contentMains[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fl_main,fragment)?.commit()
            }

            viewholder.iv_detailviewitem_comment.setOnClickListener {v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid", contentMainUidList[p1])
                startActivity(intent)
            }
            viewholder.iv_detailviewitem_detail.setOnClickListener {v ->
                var intent = Intent(v.context,DetailContentActivity()::class.java)
                intent.putExtra("contentUidD", contentMainUidList[p1])
                startActivity(intent)
            }

        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentMainUidList[position])
            firestore?.runTransaction { transaction ->


                var contentMain = transaction.get(tsDoc!!).toObject(ContentMain::class.java)

                if(contentMain!!.favorites.containsKey(uid)){
                    //when the button is clicked
                    contentMain?.favoriteCount = contentMain?.favoriteCount - 1
                    contentMain?.favorites.remove(uid)
                }else{
                    //when yhe button is not clicked
                    contentMain?.favoriteCount = contentMain?.favoriteCount + 1
                    contentMain?.favorites[uid!!] = true
                }
                transaction.set(tsDoc,contentMain)
            }
    }



    }
}
