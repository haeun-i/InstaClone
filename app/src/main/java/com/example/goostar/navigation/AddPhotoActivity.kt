package com.example.goostar.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.goostar.R
import com.example.goostar.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*


// 한 액티비티에서 후속 액티비티를 호출하고, 그 결과를 다시 메인 액티비티로 가져올 때
// startActivityForResult, onActivityResult 사용

class AddPhotoActivity : AppCompatActivity() {

    val PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // firebase 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // open the album
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            //이미지 선택시
            if (resultCode == Activity.RESULT_OK) {
                //이미지뷰에 이미지 세팅
                println(data?.data)
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            } else { // 선택하지 않고 album을 닫는 경우 activity 종료
                finish()
            }
        }
    }

    fun contentUpload(){
        // 파일 경로 생성 - 파이어베이스 기본 문법 (파이어베이스에는 파일 경로만 저장된다)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 게시글 내용 생성
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
           storageRef.downloadUrl.addOnSuccessListener { uri ->
               var contentDTO = ContentDTO()
               contentDTO.imageUrl = uri.toString()
               contentDTO.uid = auth?.currentUser?.uid
               contentDTO.userId = auth?.currentUser?.email
               contentDTO.explain = addphoto_edit_explain.text.toString()
               contentDTO.timestamp = System.currentTimeMillis()

               //게시물을 데이터를 생성 및 엑티비티 종료 -> 게시글 정보가 db에 들어간다
               firestore?.collection("images")?.document()?.set(contentDTO)

               setResult(Activity.RESULT_OK)
               finish()
           }
        }
        // !!는 null safety 제거
    }
}