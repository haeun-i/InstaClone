package com.example.goostar.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.goostar.R
import com.example.goostar.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

// Fragment는 Activity 내에서 UI의 일부분을 나타내는 요소
// 즉, 한 화면에 여러개의 화면을 보여주기위해 많이 사용
// Activity는 앱 UI의 탐색 네비게이션(구역)과 같은 전역적인 요소를 사용하고,
// 탐색 네비게이션 선택에 따라 컨텐츠가 보이는 부분은 프래그먼트로 사용 할 것을 권장한다.
// 출처: https://juahnpop.tistory.com/224 [Blacklog]

class DetailViewFragment : Fragment() {

    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        view.detailviewfragment_recyclerview.adapter = DetailRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        val contentUidList: ArrayList<String> =  arrayListOf()

        init{
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)!!
                    contentDTOs.add(item)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            // 유저 id 정보
            viewHolder.detailviewitem_profile_textview.text = contentDTOs[position].userId

            // 가운데 이미지
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)

            // 설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain

            // 좋아요 개수
            viewHolder.detailviewitem_favoritecounter_textview.text = "좋아요 " + contentDTOs[position].favoriteCount + "개"

            // 사용자 프로필 이미지 - 추가 필요
       }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

}