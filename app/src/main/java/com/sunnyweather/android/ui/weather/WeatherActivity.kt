package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*
/*
  请求天气数据,并将数据展示到界面上
  这里首先在 onCreate()方法中,从intent中取出经纬度坐标和地区名称,,并赋值到WeatherViewModel相应的变量
  中,然后对weatherLiveData对象进行观察,当获取到服务器返回的天气数据时,调用showWeatherInfo()方法进行解
  析与展示.最后调用WeatherViewModel的refreshWeather()方法来执行刷新一个天气的请求.
  showWeatherInfo()方法其实就是从Weather对象中获取数据,然后显示到相应的控件上.注意,在未来几天天气预报
  的部分,使用了一个for-in循环来处理每天的天气信息.在循环中动态加载forecast_item.xml布局并设置相应的数
  据,然后添加到父布局中.另外,生活指数方法虽然服务器会返回很多天的数据,但是界面上只需要当天的数据即可,因
  此这里对所有的生活指数都取了下标为零的那个元素的数据.设置完所有数据之后,让ScrollView变为可见状态.
*/
class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProviders.of(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        /*
           加入滑动菜单的逻辑处理
           这里主要做了两件事:第一,在切换城市按钮的点击事件中调用DrawerLayout的 openDrawer()方法来打开
           滑动菜单;第二,监听DrawerLayout的状态,当滑动菜单被隐藏的时候,,同时也要隐藏输入法.之所以要这样
           做,是因为待会在滑动菜单中搜索城市的时候会弹出输入法,而如果滑动菜单被隐藏后输入法却还留在界面上
           ,就会非常怪.
        */
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }

        })

        /*
          这里调用了getWindow().getDecorView()方法拿到当前Activity的DecorView,再调用它的setSystemUi
          Visibility()方法来改变系统UI的显示,这里传入了View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和View.
          SYSTEM_UI_FLAG_LAYOUT_STABLE就表示Activity的布局会显示在状态栏上面,最后调用一下setStatusBa
          rColor()方法将状态栏设置为透明色
        */
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng= intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat= intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName= intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            /*
              加入刷新天气的处理逻辑.首先将之前用于刷新天气信息的代码提取到一个新的 refreshWeather()方法
              中,在这里调用WeatherViewModel的refreshWeather()方法,并将SwipeRefreshLayout的isRefreshi
              ng属性设置为true,从而让下拉刷新进度条显示出来.然后在onCreate()方法中调用了SwipeRefreshLay
              out的setColorSchemeResources()方法,来设置下拉刷新进度条的颜色,这里就是使用了colors.xml中
              的colorPrimary作为进度条的颜色.接着调用setOnRefreshListener()方法给SwipeRefreshLayout设
              置一个下拉刷新的监听器,当触发了下拉刷新操作的时候,就在监听器的回调中调用refreshWeather()方
              法来刷新天气信息.
              当请求结束后,还需要将SwipeRefreshLayout的isRefreshing属性设置成false,用于表示刷新事件结束
              ,并隐藏刷新进度条.
            */
            swipeRefresh.isRefreshing = false
        })
//        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局
        val currentTempText = "${realtime.temperature.toInt()} °C"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }

}