package com.example.sysestoque

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.sysestoque.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_profile)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val btnToggleCalendar : Button = findViewById(R.id.btnToggleCalendar)
        val calendarView: CalendarView = findViewById(R.id.calendarView)

        btnToggleCalendar.setOnClickListener{
            if (calendarView.visibility == View.GONE) {
                calendarView.visibility= View.VISIBLE
            } else {
                calendarView.visibility = View.GONE
            }
        }

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            btnToggleCalendar.text = selectedDate
            calendarView.visibility = View.GONE
        }

        // fim do onCreate
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun onSaveButtonClicked(view: View) {}
}