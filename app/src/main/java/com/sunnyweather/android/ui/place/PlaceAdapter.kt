package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.place_item.view.*
/*
  准备Recyclerview的适配器
*/
/*
  从搜索城市界面跳转到天气界面
   这里在onCreateViewHolder()方法里,给place_item.xml最外层布局注册了一个点击事件监听器,然后在点击事件
   中获取当前点击项的经纬度坐标和地区名称,并把它们传入Intent中,最后调用Fragment的startActivity()方法
   启动WeatherActivity
*/
/*
 对存储与读取Place对象的功能进行具体的实现,这里需要进行两处修改:
    先把 PlaceAdapter的主构造函数中传入的Fragment对象改成PlaceFragment对象,这样就可以调用PlaceFragment
    所对应的PlaceViewModel了;接着在onCreateViewHolder()方法中,当点击了任何子项布局时,在跳转到WeatherAc
    tivity之前,先调用PlaceViewModel的savePlace()方法存储选中的城市.
*/
class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(view:View) : RecyclerView.ViewHolder(view){
        val placeName: TextView = view.findViewById(R.id.placeName)
        val placeAddress: TextView = view.findViewById(R.id.placeAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item,
            parent, false)

        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val place = placeList[position]
            /*
              处理切换城市后的逻辑,因为之前选中了某个城市后是跳转到WeatherActivity的,而现在由于本来就
              是在WeatherActivity中的,因此并不需要跳转,只要去请求新选择城市的天气信息即可.
              这里对PlaceFragment所处的Activity进行了判断,如果是在WeatherActivity中,那么就关闭滑动菜
              单,给WeatherActivity赋值新的经纬度坐标和地区名称,然后刷新城市的天气信息;而如果是在Main
              Activity中,那么就保持之前的处理逻辑不变即可.
            */
            val activity = fragment.activity
            if (activity is WeatherActivity) {
                activity.drawerLayout.closeDrawers()
                activity.viewModel.locationLng = place.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                activity.refreshWeather()
            } else {
                val intent = Intent(parent.context,WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }
                fragment.startActivity(intent)
                fragment.activity?.finish()
            }
/*            val intent = Intent(parent.context,WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            fragment.startActivity(intent)
            fragment.activity?.finish()*/
            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address
    }

    override fun getItemCount()  = placeList.size

}