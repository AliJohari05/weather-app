package com.alijt.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alijt.weatherapp.databinding.MainActivityBinding
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private var currentCity = "Tehran"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSearch.setOnClickListener {
            searchCity()
        }

        getData(currentCity)
    }

    private fun setValue(
        cityName: String,
        temp: Double,
        weatherDesc: String,
        imageUrl: String,
        sunrise: String,
        sunset: String,
        tempMin: Double,
        tempMax: Double,
        humidity: Int,
        speed: Double
    ) {
        binding.progressBar.visibility = View.GONE

        binding.textView.text = cityName
        binding.textViewtemp.text = "${kelvinToCelsius(temp).toInt()}°C"
        binding.textViewWeather.text = weatherDesc
        binding.textViewSunrise.text = "Sunrise: $sunrise"
        binding.textViewSunset.text = "Sunset: $sunset"
        binding.textViewTempMin.text = "Temp Min: ${kelvinToCelsius(tempMin).toInt()}°C"
        binding.textViewTempMax.text = "Temp Max: ${kelvinToCelsius(tempMax).toInt()}°C"
        binding.textViewhumidity.text = "Humidity: $humidity%"
        binding.textViewwind.text = "Wind: $speed m/s"

        Glide.with(this).load(imageUrl).into(binding.imageView)
    }

    private fun kelvinToCelsius(kelvin: Double) = kelvin - 273.15

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    private fun searchCity() {
        val cityName = binding.editTextCity.text.toString().trim()
        if (cityName.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
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
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Failed to get data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val rawContent = response.body!!.string()
                    getDataAndShow(rawContent)
                } else {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getDataAndShow(rawContent: String) {
        val jsonObject = JSONObject(rawContent)
        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val iconId = weatherObject.getString("icon")
        val imageUrl = "https://openweathermap.org/img/wn/${iconId}@2x.png"

        val mainObject = jsonObject.getJSONObject("main")
        val temp = mainObject.getDouble("temp")
        val tempMin = mainObject.getDouble("temp_min")
        val tempMax = mainObject.getDouble("temp_max")
        val humidity = mainObject.getInt("humidity")

        val windObject = jsonObject.getJSONObject("wind")
        val speed = windObject.getDouble("speed")

        val sysObject = jsonObject.getJSONObject("sys")
        val sunrise = formatTimestamp(sysObject.getLong("sunrise"))
        val sunset = formatTimestamp(sysObject.getLong("sunset"))

        runOnUiThread {
            setValue(
                jsonObject.getString("name"),
                temp,
                weatherObject.getString("description"),
                imageUrl,
                sunrise,
                sunset,
                tempMin,
                tempMax,
                humidity,
                speed
            )
        }
    }
}
