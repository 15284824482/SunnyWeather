package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
/*
  定义用于访问彩云天气城市搜索API的Retrofit接口
  这里在 searchPlaces()方法的上面声明了一个@GET注解,这样当调用searchPlaces()方法的时候,Retrofit就会自动
  发起一条GET请求,去访问@GET注解中配置的地址.其中搜索城市数据的API中只有query这个参数是需要动态指定的,使用
  @Query注解的方法来进行实现,另外两个参数是不会变的,因此固定写在@GET注解中.
  另外,searchPlaces()方法的返回值被声明成了Call<PlaceResponse>,这样Retrofit就会将服务器返回的JSON数据自
  动解析成PlaceResponse对象了.
*/
interface PlaceService {

    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query") query:String) : Call<PlaceResponse>


}