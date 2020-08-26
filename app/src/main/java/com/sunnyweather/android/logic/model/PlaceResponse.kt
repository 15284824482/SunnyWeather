package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
/*
   定义数据模型.
  这里完全按照搜索城市数据接口返回的JSON格式来定义的.不过JSON中一些字段的命名可能与Kotlin的命名规范不一致,
  因此这里使用了一个 @SerializedName注解,来让JSON字段和Kotlin字段之间建立映射关系.
*/
data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(val name: String, val location: Location,
    @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)