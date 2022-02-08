package com.example.goostar.navigation

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
import com.example.goostar.LoginActivity
import com.example.goostar.MainActivity
import com.example.goostar.R
import com.example.goostar.navigation.model.ContentDTO
import com.example.goostar.navigation.model.FollowDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class UserFragment : Fragment() {

    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String ?= null
    var currentUserUid: String? = null
    var auth : FirebaseAuth?= null

    var followListenerRegistration: ListenerRegistration? = null
    var followingListenerRegistration: ListenerRegistration? = null

    companion object { // static 선언?
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        uid = arguments?.getString("destinationUid")

        if (uid != null && uid == currentUserUid) { // 나의 user 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        }else{ // 다른 사람의 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)

            // 선택한 사람의 아이디에 맞게 툴바 수정
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }

            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back.visibility = View.VISIBLE
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(requireActivity(), 3)
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        getFollowing()
        getFollower()
        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // contentDTO를 담은 arrayList 초기화
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(querySnapshot == null) return@addSnapshotListener // querysnapshot 이면 바로 종료

                for (snapshot in querySnapshot?.documents!!) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                account_tv_post_count.text = contentDTOs.size.toString()
                notifyDataSetChanged() // recycleView 새로고침
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3

            // width만큼 이미지뷰 생성
            val imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        }
    }

     //Follow 신청
    fun requestFollow() {


            var tsDocFollowing = firestore!!.collection("users").document(currentUserUid!!)
            firestore?.runTransaction { transaction ->

                var followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO::class.java)
                if (followDTO == null) {

                    followDTO = FollowDTO()
                    followDTO.followingCount = 1
                    followDTO.followings[uid!!] = true

                    transaction.set(tsDocFollowing, followDTO)
                    return@runTransaction

                }
                // Unstar the post and remove self from stars
                if (followDTO?.followings?.containsKey(uid)!!) {

                    followDTO?.followingCount = followDTO?.followingCount - 1
                    followDTO?.followings.remove(uid)
                } else {

                    followDTO?.followingCount = followDTO?.followingCount + 1
                    followDTO?.followings[uid!!] = true
                }
                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            var tsDocFollower = firestore!!.collection("users").document(uid!!)
            firestore?.runTransaction { transaction ->

                var followDTO = transaction.get(tsDocFollower).toObject(FollowDTO::class.java)
                if (followDTO == null) {

                    followDTO = FollowDTO()
                    followDTO!!.followerCount = 1
                    followDTO!!.followers[currentUserUid!!] = true


                    transaction.set(tsDocFollower, followDTO!!)
                    return@runTransaction
                }

                if (followDTO?.followers?.containsKey(currentUserUid!!)!!) {


                    followDTO!!.followerCount = followDTO!!.followerCount - 1
                    followDTO!!.followers.remove(currentUserUid!!)
                } else {

                    followDTO!!.followerCount = followDTO!!.followerCount + 1
                    followDTO!!.followers[currentUserUid!!] = true

                }// Star the post and add self to stars

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

        }

    fun getFollowing() {
        followingListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView!!.account_tv_following_count.text = followDTO?.followingCount.toString()
        }
    }


    fun getFollower() {
        followListenerRegistration = firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            val followDTO = documentSnapshot?.toObject(FollowDTO::class.java)
            if (followDTO == null) return@addSnapshotListener
            fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount.toString()
            if (followDTO?.followers?.containsKey(currentUserUid)!!) {

                fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                fragmentView?.account_btn_follow_signout
                    ?.background
                    ?.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
            } else {

                if (uid != currentUserUid) {

                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                }
            }

        }

    }

    // 올린 이미지를 다운로드받는 함수
    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { value, error ->
            // 프로필사진을 실시간으로 체크하기 위해서 snapshot으로 받아옴
            if(value == null) return@addSnapshotListener
            if(value.data != null) {
                var url = value?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions.circleCropTransform()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }
}