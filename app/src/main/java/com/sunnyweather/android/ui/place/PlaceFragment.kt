package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.R
import com.sunnyweather.android.ui.weather.WeatherActivity
import kotlinx.android.synthetic.main.fragment_place.*

/*
  实现Fragment
  首先这里使用了lazy函数这种懒加载技术获取PlaceViewModel的实例,这是一种非常棒的写法,允许我们在整个类中
  随时使用viewModel这个变量,而完全不用关心它何时初始化,是否为空等前提条件.
  接下来在 onCreateView()方法中加载了fragment_place布局.这是Fragment的标准用法.
  最后在onActivityCreated()方法中.先是给Recyclerview设置了LayoutManager和和适配器,并使用PlaceViewMo
  del中的placeList集合作为数据源.紧接着调用了EditText的addTextChangedListener()方法来监听搜索框内容的
  变化情况.每当搜索框中的内容发生了变化,就获取新的内容,然后传递给PlaceViewModel的searchPlace()方法,这样
  就可以发起搜索城市数据的网咯请求了.而当输入搜索框中的内容为空时,就将Recyclerview隐藏起来,同时将那张仅用
  于美观用途的背景图显示出来.
  之后为了能获取的服务器响应的数据,借助LiveData完成.这里对PlaceViewModel中的PlaceLiveData对象进行观察,当
  有任何数据发生变化时,就会回调到Observer接口实现中.然后我们会对回调的数据进行观察:如果数据不为空,那么就将
  这些数据添加到PlaceViewModel的placeList集合中,并通知PlaceAdapter刷新界面;如果数据为空,则说明发生了异常,
  此时弹出一个Toast提示,并将具体的异常原因打印出来
*/
/*
 完成了存储功能后,对存储的状态进行判断和读取.在PlaceFragment的onActivityCreated()方法中进行判断.如果
 当前已有存储的城市数据,那么就获取已存储的数据并解析成Place对象,然后使用它的经纬度坐标和城市名直接跳转并
 传递给WeatherActivity,这样就不用每次都重新搜索并选择城市了.
*/
class PlaceFragment : Fragment() {

    val viewModel by lazy { ViewModelProviders.of(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.isPlaceSaved()) {
            val place = viewModel.getSavePlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        recyclerView.adapter = adapter
        searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)
            } else {
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {
                recyclerView.visibility = View.VISIBLE
                bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })

    }

}