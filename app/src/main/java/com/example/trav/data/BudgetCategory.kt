package com.example.trav.data

object BudgetCategory {
    // [수정] 여행 계획에 꼭 필요한 핵심 4가지로 축소
    val mainCategories = listOf("교통", "식사", "관광", "기타")

    // [수정] 소분류도 이에 맞춰 정리
    val subCategories = mapOf(
        "교통" to listOf("항공", "기차", "버스", "지하철", "택시", "렌터카"),
        "식사" to listOf("식당", "카페", "주점", "편의점/간식"),
        "관광" to listOf("명소", "투어", "쇼핑", "액티비티", "휴식"),
        "기타" to listOf("체크인/아웃", "준비", "이동", "기타")
    )
}