package com.sunnyweather.android.logic.model
/*
   定义 Weather,用于封装Realitem和Daily对象.
*/
data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)
