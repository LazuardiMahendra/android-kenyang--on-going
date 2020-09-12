package com.example.kenyangproject.Navigatinon

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kenyangproject.LoginActivity
import com.example.kenyangproject.MainActivity
import com.example.kenyangproject.Model.ContentMain
import com.example.kenyangproject.Model.FollowMain
import com.example.kenyangproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import  kotlinx.android.synthetic.main.fragment_profil.view.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.view_list_detail.view.*
import kotlinx.android.synthetic.main.view_list_detail_resep.view.*
import kotlinx.android.synthetic.main.view_list_user.view.*


class ProfilViewFragment : Fragment() {

    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserId: String? = null

    companion object {
        var PICKER_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?
        , savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_profil, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserId = auth?.currentUser?.uid
        if (uid == currentUserId) {
            //My Page
            fragmentView?.bt_acc_follow_signout?.text = getString(R.string.signout)
            fragmentView?.bt_acc_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            //other user page
            fragmentView?.bt_acc_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.tb_main_username?.text = arguments?.getString("userId")
            mainactivity?.tb_bt_back?.setOnClickListener {
                mainactivity.bn_main.selectedItemId = R.id.home_menu
            }
            mainactivity?.tb_bt_back?.visibility = View.VISIBLE
            mainactivity?.tb_main_username?.visibility = View.VISIBLE
            fragmentView?.bt_acc_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.rv_account?.adapter = UserViewAdapter()
        fragmentView?.rv_account?.layoutManager = GridLayoutManager(activity!!, 3)

        //pick photo profile from gallery
        fragmentView?.tv_edit_photo?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICKER_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        getFollowersAndFollowing()
        return fragmentView
    }

    //get image from firestore to photo profile

    fun getProfileImage() {
        firestore?.collection("profileImage")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null) {
                    var url = documentSnapshot?.data!!["image"] //image not images
                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView?.iv_account_profile!!)
                }
            }
    }

    fun getFollowersAndFollowing() {
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var followMain = documentSnapshot.toObject(FollowMain::class.java)
                if (followMain?.followingCount != null) {
                    fragmentView?.tv_following_count?.text = followMain?.followingCount?.toString()
                }
                if (followMain?.followerCount != null) {
                    fragmentView?.tv_follower_count?.text = followMain?.followerCount?.toString()
                    if (followMain?.followers?.containsKey(currentUserId!!)) {
                        fragmentView?.bt_acc_follow_signout?.text =
                            getString(R.string.follow_cancel)
                        fragmentView?.bt_acc_follow_signout?.background?.setColorFilter(
                            ContextCompat.getColor(activity!!, R.color.colorLightGray),
                            PorterDuff.Mode.MULTIPLY
                        )
                    } else {
                        if (uid != currentUserId) {
                            fragmentView?.bt_acc_follow_signout?.text = getString(R.string.follow)
                            fragmentView?.bt_acc_follow_signout?.background?.colorFilter = null
                        }
                    }
                }
            }
    }

    fun requestFollow() {
        //save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserId!!)
        firestore?.runTransaction { transaction ->
            var followMain = transaction.get(tsDocFollowing!!).toObject(FollowMain::class.java)
            if (followMain == null) {
                followMain == FollowMain()
                followMain!!.followingCount = 1
                followMain!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followMain)
                return@runTransaction
            }

            if (followMain!!.following.containsKey(uid)) {
                // it remove following third person  when  a third person follow me
                followMain?.followingCount = followMain?.followingCount - 1
                followMain?.followers?.remove(uid!!)
            } else {
                // it add following third person  when  a third person follow me
                followMain?.followingCount = followMain?.followingCount + 1
                followMain?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followMain)
            return@runTransaction
        }
        //save data to third person
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followMain = transaction.get(tsDocFollower!!).toObject(FollowMain::class.java)
            if (followMain == null) {
                followMain = FollowMain()
                followMain!!.followerCount = 1
                followMain!!.followers[currentUserId!!] = true

                transaction.set(tsDocFollower, followMain!!)
                return@runTransaction
            }
            if (followMain!!.followers.containsKey(currentUserId)) {
                // it cancel followers when i a third person
                followMain!!.followerCount = followMain!!.followerCount - 1
                followMain!!.followers.remove(currentUserId!!)
            } else {
                // it add followers when i a third person
                followMain!!.followerCount = followMain!!.followerCount + 1
                followMain!!.followers[currentUserId!!] = true
            }
            transaction.set(tsDocFollower, followMain!!)
            return@runTransaction
        }

    }

    inner class UserViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentMains: ArrayList<ContentMain> = arrayListOf()
        var contentMainUidList : java.util.ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //somtimes, this code return null of querysnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener
                    //get data
                    for (snapshot in querySnapshot.documents) {
                        contentMains.add(snapshot.toObject(ContentMain::class.java)!!)
                        contentMainUidList.add(snapshot.id)
                    }
                    fragmentView?.tv_post_count?.text = contentMains.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }
        inner class CustomViewHolder(var imageview: ImageView) :
            RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentMains.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

            var imageview = (p0 as CustomViewHolder).imageview

            Glide.with(p0.itemView.context).load(contentMains[p1].resepImage)
                .apply(RequestOptions().centerCrop()).into(imageview)

        }

    }

//    inner class  DetailViewProfileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//        var contentMains : java.util.ArrayList<ContentMain> = arrayListOf()
//        var contentMainUidList : java.util.ArrayList<String> = arrayListOf()
//
//        init{
//
//            firestore?.collection("images")?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
//                contentMains.clear()
//                contentMainUidList.clear()
//                //somtimes, this code return null of querysnapshot when it signout
//                if(querySnapshot == null) return@addSnapshotListener
//                for(snapshot in querySnapshot!!.documents){
//                    var item = snapshot.toObject(ContentMain::class.java)
//                    contentMains.add(item!!)
//                    contentMainUidList.add(snapshot.id)
//                }
//                notifyDataSetChanged()
//            }
//        }
//
//
//
//        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
//            var view = LayoutInflater.from(p0.context).inflate(R.layout.view_list_detail,p0,false)
//            return CustomViewHolder(view)
//        }
//
//        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)
//
//        override fun getItemCount(): Int {
//            return  contentMains.size
//        }
//
//        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
//            var viewholder = (p0 as CustomViewHolder).itemView
//
//            viewholder.tv_profile_username.text = contentMains!![p1].userId
//        }
//
//
//    }
}
