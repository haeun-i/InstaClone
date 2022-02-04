package com.example.goostar.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.goostar.R

// Fragment는 Activity 내에서 UI의 일부분을 나타내는 요소
// 즉, 한 화면에 여러개의 화면을 보여주기위해 많이 사용
// Activity는 앱 UI의 탐색 네비게이션(구역)과 같은 전역적인 요소를 사용하고,
// 탐색 네비게이션 선택에 따라 컨텐츠가 보이는 부분은 프래그먼트로 사용 할 것을 권장한다.
// 출처: https://juahnpop.tistory.com/224 [Blacklog]

class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        return view
    }
}