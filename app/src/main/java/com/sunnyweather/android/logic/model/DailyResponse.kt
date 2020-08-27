package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
import java.util.*

/*
   未来几天天气信息的数据模型
   这次我们将所有的数据模型类都定义在了DailyResponse的内部,这里虽然它和RealtimeResponse内部都包含了
   一个Result类,但是它们之间是完全不冲突的.
*/
data class DailyResponse(val status:String,val result:Result){

    data class Result(val daily: Daily)

    data class Daily(val temperature: List<Temperature>, val skycon: List<Skycon>,
        @SerializedName("life_index") val lifeIndex: LifeIndex)

    data class Temperature(val max: Float, val min: Float)

    data class Skycon(val value: String, val date: Date)

    data class LifeIndex(val coldRisk: List<LifeDescription>, val carWashing: List<LifeDescription>,
        val ultraviolet: List<LifeDescription>, val dressing: List<LifeDescription>)

    data class LifeDescription(val desc: String)

}