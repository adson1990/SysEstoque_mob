package com.example.sysestoque

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.sysestoque.R.color.white
import com.example.sysestoque.data.database.ColorDatabaseHelper
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.databinding.ActivityProfileBinding
import com.example.sysestoque.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileBinding
    private lateinit var textView: TextView
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var dbHelper: ColorDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private var activeColor = "red"

    /*private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0*/

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Lidando com cliques no menu
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this,"Dados descartados!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                R.id.nav_profile -> {
                    // permanece na mesma tela
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_settings -> {
                    // Abrir tela de Configurações
                }
                R.id.nav_exit -> {
                    logout()
                    drawerLayout.closeDrawers() // Fecha o drawer após a ação
                    return@setNavigationItemSelectedListener true
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        dbHelper = ColorDatabaseHelper(this)

        textView = findViewById(R.id.textView)
        seekBarRed = findViewById(R.id.seekBarRed)
        seekBarGreen = findViewById(R.id.seekBarGreen)
        seekBarBlue = findViewById(R.id.seekBarBlue)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btnRed -> {
                    activeColor = "red"
                    toggleSeekBarVisibility(seekBarRed, seekBarGreen, seekBarBlue)
                }
                R.id.btnGreen -> {
                    activeColor = "green"
                    toggleSeekBarVisibility(seekBarGreen, seekBarRed, seekBarBlue)
                }
                R.id.btnBlue -> {
                    activeColor = "blue"
                    toggleSeekBarVisibility(seekBarBlue, seekBarRed, seekBarGreen)
                }
            }
        }

        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTextColor(textView)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekBarRed.setOnSeekBarChangeListener(seekBarChangeListener)
        seekBarGreen.setOnSeekBarChangeListener(seekBarChangeListener)
        seekBarBlue.setOnSeekBarChangeListener(seekBarChangeListener)

        /*17/10/2024 - >A cor do textview mesmo na metade da barra estava muito escura, o textview começava
        * com a cor preta e demorava a ganha cor a medida que o seekbar avançava.*/
        /*setupSeekBar(seekBarRed) { value ->
            redValue = value
            updateTextViewColor()
        }
        setupSeekBar(seekBarGreen) { value ->
            greenValue = value
            updateTextViewColor()
        }
        setupSeekBar(seekBarBlue) { value ->
            blueValue = value
            updateTextViewColor()
        }*/

        val btnToggleCalendar : Button = findViewById(R.id.btnToggleCalendar)
        val calendar = Calendar.getInstance()

        /*Calendário retrocedia mês a mês não podendo escolher ano
        o que causava uma experiência ruim em escolher datas muito passadas
        val calendarView: CalendarView = findViewById(R.id.calendarView)
        calendarView.visibility = View.GONE*/

        btnToggleCalendar.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    btnToggleCalendar.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        //CALENDÁRIO RETIRADO DEVIDO FALTA DE USABILIDADE EM RETROCEDER DATAS
        /*calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            btnToggleCalendar.text = selectedDate
            calendarView.visibility = View.GONE
        }*/

        val colors = dbHelper.getColors()
        colors?.let { (red, green, blue) ->
            seekBarRed.progress = red
            seekBarGreen.progress = green
            seekBarBlue.progress = blue

            if (red == 0 && green == 0 && blue == 0) {
                textView.setTextColor(Color.WHITE)
            } else {
                updateTextColor(textView)
            }
        }

        // fim do onCreate
    }

    private fun toggleSeekBarVisibility(
        visibleSeekBar: SeekBar,
        seekBar2: SeekBar,
        seekBar3: SeekBar
    ) {
        visibleSeekBar.visibility = SeekBar.VISIBLE
        seekBar2.visibility = SeekBar.GONE
        seekBar3.visibility = SeekBar.GONE
    }

    private fun updateTextColor(textView: TextView) {
        val red = seekBarRed.progress
        val green = seekBarGreen.progress
        val blue = seekBarBlue.progress

        //maneira de melhorar a cor para deixá-la mais viva
        val hsv = FloatArray(3)
        Color.RGBToHSV(red, green, blue, hsv)
        hsv[1] = 1.0f // Saturação máxima para manter a cor viva
        hsv[2] = 0.8f // Brilho elevado para evitar cores muito escuras

        val vibrantColor = Color.HSVToColor(hsv)
        if(red == 0 && green == 0 && blue == 0){
            textView.setTextColor(Color.WHITE)
        }else {
        textView.setTextColor(vibrantColor)
        }
        dbHelper.saveColors(red, green, blue) // salvando no DB do android as cores
    }

    /*A cor do textview não estava viva o suficiente, ficando muito escura mesmo usando seekbar até o fim
    mudança na forma de alteração de cor usando HSV (Hue, Saturation, Value) para clarear mais as cores*/
    /*private fun setupSeekBar(seekBar: SeekBar, onValueChanged: (Int) -> Unit) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onValueChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun hideAllSeekBars() {
        seekBarRed.visibility = View.GONE
        seekBarGreen.visibility = View.GONE
        seekBarBlue.visibility = View.GONE
    }
    private fun updateTextViewColor() {
        val color = Color.rgb(redValue, greenValue, blueValue)
        textView.setTextColor(color)
    }*/

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_profile)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun onSaveButtonClicked(view: View) {}

    private fun logout(){
        val dbHelperLogin = DbHelperLogin(this)
        dbHelperLogin.lembrarCliente(false)
        Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        chamarLoginActivity()
    }

    private fun chamarLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun abrirDashboradActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Fecha o Drawer ao pressionar o botão de voltar
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}