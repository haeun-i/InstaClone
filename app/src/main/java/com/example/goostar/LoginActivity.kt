package com.example.goostar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null // auth 라이브러리 불러오기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance() // firebase 불러오기
        email_login_button.setOnClickListener {
            signinAndSignup()
        }
    }

    fun signinAndSignup() {
        // ?.는 변수가 null이면 메서드 호출이 무시되고 null이 결과 값이 된다.
        //변수가 null이 아니면 메서드를 정상 실행하고 결과값을 얻어온다.
        // !!는 null이 아님을 보여주는 역할
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), passwrod_edittext.text.toString())
            ?.addOnCompleteListener {
                    task->
                if(task.isSuccessful){
                    // 회원가입이 되었을 때
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()) {
                    // null값이나 비어 있으면 에러 발생하고 에러 메시지 출력
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    // 이미 계정이 있는 경우 로그인 실행
                    signinEmail()
                }
            }
    }

    fun signinEmail(){
        auth!!.signInWithEmailAndPassword(email_edittext.text.toString(), passwrod_edittext.text.toString())
            ?.addOnCompleteListener {
                    task->
                if(task.isSuccessful){
                    // id, pw가 일치 했을 때 로그인되고 메인페이지 이동
                    moveMainPage(task.result?.user)
                }else{
                    // 로그인이 실패하였을 때 에러 메시지 출력
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user!= null){
            startActivity(Intent(this,MainActivity::class.java)) // main activity 이동 코드
            finish() // 이동하고 로그인 액티비티는 꺼진다
        }
    }

    override fun onStart() {
        super.onStart()

        //자동 로그인 설정 - 로그인 되어 있으면 메인 페이지로 이동
        moveMainPage(auth?.currentUser)

    }
}