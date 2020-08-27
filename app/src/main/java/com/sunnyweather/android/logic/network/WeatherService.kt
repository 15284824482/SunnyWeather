package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/*
   定义用于访问天气信息API的Retrofit接口.
   这里定义了两个方法:getRealtimeWeather用于获取实时的天气信息,getDailyWeather()方法用于获取未来的天气
   信息.在每个方法的上面仍然还是使用@GET注解来声明要访问的API接口,并且还使用了@Path注解来向请求接口中动态
   传入经纬度的坐标.这两个方法的返回值分别被声明成了 Call<RealtimeResponse>和Call<DailyResponse>,对应
   刚刚定义好的两个数据模型类.
*/
interface WeatherService {

    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<RealtimeResponse>

    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<DailyResponse>

}