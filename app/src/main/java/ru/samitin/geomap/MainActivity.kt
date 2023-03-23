package ru.samitin.geomap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.samitin.geomap.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.container.id, MapsFragment.newInstance())
                .commitNow()
        }
    }
}