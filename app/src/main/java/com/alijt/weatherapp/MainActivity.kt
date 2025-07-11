package com.alijt.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alijt.weatherapp.databinding.MainActivityBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    lateinit var binding: MainActivityBinding
    private var currentCity: String = "Tehran"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the search button click listener
        binding.buttonSearch.setOnClickListener {
            searchCity(it)
        }

        getData("Tehran") // Default city
    }

    fun setValue(cityName: String, temp: Double, weatherDesc: String, imageUrl: String,
                 sunrise: String, sunset: String,
                 tempMin: Double, tempMax: Double, pressure: Int, humidity: Int,
                 seaLevel: Int, grndLevel: Int, speed: Double, deg: Int
    ) {
        binding.progressBar.visibility = View.INVISIBLE
        binding.imageViewreload.visibility = View.VISIBLE
        binding.textView.text = cityName
        binding.textViewtemp.text = kelvinToCelsius(temp).toInt().toString()
        binding.textViewWeather.text = weatherDesc
        binding.textViewSunrise.text = "Sunrise: $sunrise"
        binding.textViewSunset.text = "Sunset: $sunset"
        binding.textViewTempMin.text = "Temp Min: ${kelvinToCelsius(tempMin).toInt()}"
        binding.textViewTempMax.text = "Temp Max: ${kelvinToCelsius(tempMax).toInt()}"
        binding.textViewpressure.text = "Pressure: $pressure"
        binding.textViewhumidity.text = "Humidity: $humidity"
        binding.textViewsealevel.text = "Sea Level: $seaLevel"
        binding.textViewgrndlevel.text = "Grnd Level: $grndLevel"
        binding.textViewwind.text = "Speed: $speed"
        binding.textViewdeg.text = "Degree: $deg"

        Glide.with(this)
            .load(imageUrl)
            .into(binding.imageView)
    }

    fun kelvinToCelsius(kelvin: Double): Double {
        return kelvin - 273.15
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    fun searchCity(view: View) {
        val cityName = binding.editTextCity.text.toString()
        if (cityName.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            binding.imageViewreload.visibility = View.INVISIBLE
            getData(cityName)
            currentCity = cityName
        } else {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getData(cityName: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=46c8b4f7f8450408e67c90a0e80cd259")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Request failed: ${e.message}")
                runOnUiThread {
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@MainActivity, "Failed to get data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val rawContent = response.body!!.string()
                    getDataAndShowThem(rawContent)
                } else {
                    runOnUiThread {
                        binding.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getDataAndShowThem(rawContent: String) {
        val jsonObject = JSONObject(rawContent)
        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val iconId = weatherObject.getString("icon")
        val imageUrl = "https://openweathermap.org/img/wn/${iconId}@2x.png"
        val mainObject = jsonObject.getJSONObject("main")
        val tempMin = mainObject.getDouble("temp_min")
        val tempMax = mainObject.getDouble("temp_max")
        val pressure = mainObject.getInt("pressure")
        val humidity = mainObject.getInt("humidity")
        val seaLevel = mainObject.getInt("sea_level")
        val grndLevel = mainObject.getInt("grnd_level")
        val sysObject = jsonObject.getJSONObject("sys")
        val windObject = jsonObject.getJSONObject("wind")
        val speed = windObject.getDouble("speed")
        val deg = windObject.getInt("deg")

        val sunriseTimestamp = sysObject.getLong("sunrise")
        val sunsetTimestamp = sysObject.getLong("sunset")

        val sunriseTime = formatTimestamp(sunriseTimestamp)
        val sunsetTime = formatTimestamp(sunsetTimestamp)

        runOnUiThread {
            setValue(jsonObject.getString("name"), mainObject.getDouble("temp"),
                weatherObject.getString("description"), imageUrl,
                sunriseTime, sunsetTime, tempMin, tempMax, pressure, humidity, seaLevel, grndLevel, speed, deg)
        }
    }

    fun reloadData(view: View) {
        binding.imageViewreload.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.textView.text = "--"
        binding.textViewtemp.text = "--"
        binding.textViewWeather.text = "--"
        binding.textViewSunrise.text = "--"
        binding.textViewSunset.text = "--"
        binding.textViewTempMin.text = "--"
        binding.textViewTempMax.text = "--"
        binding.textViewpressure.text = "--"
        binding.textViewhumidity.text = "--"
        binding.textViewsealevel.text = "--"
        binding.textViewgrndlevel.text = "--"
        binding.textViewwind.text = "--"
        binding.textViewdeg.text = "--"
        Glide.with(this)
            .load(R.drawable.baseline_refresh_24)
            .into(binding.imageView)
        getData(currentCity)
    }
}
