package com.sunnyweather.android.ui.place


import android.service.autofill.Transformation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

/*
  定义ViewModel层
  首先PlaceViewModel中也定义了一个searchPlaces()方法,但是这里并未直接调用仓库层中的searchPlaces()方法,
  而是将传入的搜索参数赋值给了一个searchLiveData对象,并使用Transformations的switchMap()方法来观察这个
  对象.否则仓库层返回的LiveData对象将无法进行观察.现在每当searchPlaces()函数被调用时,switchMap()方法所
  对应的转换函数就会执行.然后在转换函数中,只需要调用仓库层中定义的searchPlaces()方法就可以发起网咯请求.同
  时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象.
  另外还在PlaceViewModel中定义了一个placeList集合,用于对界面上显示的城市数据进行缓存,因为原则上与界面相关
  的数据都应该放到ViewModel中,这样可以保证它们在手机屏幕发生旋转的时候不会丢失.
*/
class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val  placeLiveData = Transformations.switchMap(searchLiveData) { query->
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }


}