package com.example.trav.data

object BudgetCategory {
    val mainCategories = listOf("기타", "교통", "관광", "식사")

    // [수정] 요청하신 순서: 기타 -> 준비 -> 교통 -> 관광 -> 식사 -> 숙소
    val budgetDisplayCategories = listOf("기타", "준비", "교통", "관광", "식사", "숙소")

    val subCategories = mapOf(
        "숙소" to emptyList(),
        "준비" to listOf("여행자 보험", "유심/이심", "환전", "항공권", "기타"),

        "교통" to listOf("항공", "기차", "버스", "지하철", "택시", "렌터카"),
        "관광" to listOf("명소", "투어", "쇼핑", "액티비티", "휴식"),
        "식사" to listOf("식당", "카페", "주점", "편의점/간식"),
        "기타" to emptyList()
    )

    val currencyMap = mapOf(
        "KRW" to "대한민국", "USD" to "미국", "JPY" to "일본", "EUR" to "유럽연합",
        "CNY" to "중국", "GBP" to "영국", "CHF" to "스위스", "CAD" to "캐나다",
        "AUD" to "호주", "HKD" to "홍콩", "TWD" to "대만", "THB" to "태국",
        "VND" to "베트남", "PHP" to "필리핀"
    )
    val currencies = currencyMap.keys.toList()
}