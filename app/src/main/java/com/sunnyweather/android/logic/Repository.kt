package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
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
*/
object Repository {
 /*   fun searchPlaces(query:String) = liveData(Dispatchers.IO){
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
    }*/

    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
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