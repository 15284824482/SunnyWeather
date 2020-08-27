package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext


/*
  定义仓库层的统一封装入口
  一般在仓库中定义的方法,为了能将异步获取的数据以响应式编程的方式通知给上一层,通常会返回一个LiveData对象.
  这里使用了一个LiveData新的技巧,这里的liveData()函数是lifecycle-liveData-ktx库提供的一个非常强大且好
  用的功能,它可以自动构建并返回一个liveData对象,然后在它的代码块中提供一个挂起函数的上下文,这样就可以在l
  iveData()函数的代码块中调用任意的挂起函数了,这里调用了SunnyWeatherNetwork的searchPlaces()函数来搜索
  城市数据,然后判断如果服务器响应的状态是ok,那么就是要Kotlin内置的Result.success()方法来包装获取的城市
  数据列表,否则就使用Result.failure()方法来包装一个异常信息.最后使用一个emit()方法将包装的结果发射出去,
  这个emit()方法其实类似于LiveData的setValue()方法来通知数据变化,只不过这里无法直接取得返回的LiveData
  对象,所有lifecycle-liveData-ktx库提供了这样一个替代方法.
  注意:这里还将liveData()函数的线程类型参数指定成了Dispatchers.IO这样代码中的所有代码就都运行在子线程中
  了.
  fun searchPlaces(query:String) = liveData(Dispatchers.IO){
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
*/
/*
   注意:在仓库层我们并没有提供两个分别用于获取实时天气和未来天气的方法,而是提供了一个refreshWeather()方
   法用于刷新天气信息.因为对于调用方法而言,需要调用两次请求才能获得其想要的所有天气信息数据明显是比较繁琐
   的行为.因此最好的做法就是在仓库层再进行一次统一的封装.
   不过获取实时天气和未来天气信息这两个请求是没有先后顺序的,因此让它们并发执行提示程序的运行效率,为了在同
   时得到它们的响应数据后才进一步执行程序,使用async函数,async函数的作用:只需要分别在两个async函数中发起
   网咯请求,然后再分别调用它们的await()方法,就可以保证只有在两个网咯请求都成功响应之后,才会进一步执行程
   序.另外async函数必须在协程作用域内才能使用,所有这里又使用coroutineScope函数创建了一个协程作用域.
   接下来,在同时获取到RealtimeResponse和DailyResponse之后,如果它们的响应状态都是ok,那么就将Realtime和
   Daily对象取出并封装到一个Weather对象中,然后使用Result.success()方法来包装这个Weather对象,否则就使用
   Result.failure()方法来包装一个异常信息.最后调用emit()方法将包装的结果发射出去.
    fun refreshWeather(lng:String,lat:String) = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(
                        realtimeResponse.result.realtime,
                        dailyResponse.result.daily
                    )
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" +
                                    "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure<Weather>(e)
        }
        emit(result)
    }
*/
/*
  这里由于我们使用了协程来简化网咯回调的写法,导致 SunnyWeatherNetwork中封装的每个网咯请求接口都可能会抛出
  异常,于是必须在仓库层中为每个网咯请求都进行try  catch处理,增加了仓库层代码实现的复杂度.然而,其实完全可以
  在某个统一的入口函数中进行封装,使得只有进行一次try catch处理即可.
  这段代码最核心的地方就在于我们新增的fire()函数,这是一个按照liveData()函数的参数接收标准定义的一个高阶函数
  .在fire()函数的内部会先调用一下liveData()函数,然后在liveData()函数的代码块中统一进行try catch处理,并在
  try语句中调用传入的Lambda表达式中的代码,最终获取Lambda表达式的执行结果并调用emit()方法发射出去.
  另外需要注意,在liveData()函数的代码块中,我们是拥有挂起函数上下文的,可是回调到Lambda表达式中,代码就没有挂
  起函数上下文了,但实际上Lambda表达式中的代码一定也是在挂起函数中运行的,为了解决这个问题,需要在函数类型参数
  前声明一个suspend关键字,以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的.
  定义好了fire()函数之后.只需要分别将searchPlaces()和refreshWeather()方法中调用的liveData()函数替换成fi
  re()函数,然后把诸如try catch语句,emit()方法之类的逻辑移除即可.
*/
/*
   实现PlaceDap.这里只是做了一层封装而已, 其实这里的实现方式并不标准,因为即使是对SharedPreferences文件进
   行读写的操作,也是不太建议在主线程中进行,虽然它的执行效果通常会很快.最佳的实现方式还是开启一个线程来执行
   这些比较耗时的任务,然后再通过LiveData对象进行数据返回.
*/
object Repository {

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavePlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(
                    realtimeResponse.result.realtime,
                    dailyResponse.result.daily
                )
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

}


