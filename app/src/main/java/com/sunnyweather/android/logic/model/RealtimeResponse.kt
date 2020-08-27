package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/*
  定义实时天气的数据模型.
  注意:这里将所有的数据模型类都定义在了 RealtimeResponse内部,这样可以防止出现和其他接口的数据模型类有同名
  的冲突.
*/

data class RealtimeResponse(val status: String, val result: Result){

    data class Result(val realtime: Realtime)

    data class Realtime(val skycon: String, val temperature: Float,
                        @SerializedName("air_quality") val airQuality: AirQuality)

    data class AirQuality(val aqi: AQI)

    data class AQI(val chn: Float)

}

