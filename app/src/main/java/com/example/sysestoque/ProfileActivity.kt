package com.example.sysestoque

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.example.sysestoque.backend.AuthApiClient
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.Client
import com.example.sysestoque.data.database.ColorDatabaseHelper
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.databinding.ActivityProfileBinding
import com.example.sysestoque.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProfileBinding
    private lateinit var textView: TextView
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var ftCliente : ImageView
    private lateinit var nomeCompleto : EditText
    private lateinit var cpfClient : EditText
    private lateinit var emailCLiente: EditText
    private lateinit var senhaCliente : EditText
    private lateinit var sexoCliente : TextView
    private lateinit var aniversarioCliente : Button
    private lateinit var salarioCliente : EditText
    private lateinit var enderecoCliente : EditText
    private lateinit var bairroCliente : EditText
    private lateinit var numCasaCliente : EditText
    private lateinit var cepCliente : EditText
    private lateinit var cidadeCliente : EditText
    private lateinit var estadoCliente : EditText
    private lateinit var dddCiente : EditText
    private lateinit var celularCliente : EditText
    private lateinit var dbHelper: ColorDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var authRepository: AuthRepository

    private var activeColor = "red"

    /*private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0*/

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ftCliente = findViewById(R.id.ftUsuario)
        nomeCompleto = findViewById(R.id.edtNomeCompleto)
        cpfClient = findViewById(R.id.edtCPF)
        emailCLiente = findViewById(R.id.edtEmail)
        senhaCliente = findViewById(R.id.edtSenha)
        sexoCliente = findViewById(R.id.textView5)
        aniversarioCliente = findViewById(R.id.btnToggleCalendar)
        salarioCliente = findViewById(R.id.edtSalario)
        enderecoCliente = findViewById(R.id.edtEndereco)
        bairroCliente = findViewById(R.id.edtBairro)
        numCasaCliente = findViewById(R.id.edtNumCasa)
        cepCliente = findViewById(R.id.edtCEP)
        cidadeCliente = findViewById(R.id.edtCidade)
        estadoCliente = findViewById(R.id.edtEstado)
        dddCiente = findViewById(R.id.edtDDD)
        celularCliente = findViewById(R.id.edtNumeroCel)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository()

        // menu lateral na tela
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

        fun getDadosCliente(idCliente: Long, token: String){
            authRepository.getClientById(idCliente, token, object : Callback<Client>{
                override fun onResponse(call: Call<Client>, response: Response<Client>) {
                    if (response.isSuccessful){
                        nomeCompleto.setText(response.body()?.name)
                        cpfClient.setText(response.body()?.cpf)
                       // salarioCliente.setText(response.body()?.income)
                        if (response.body()?.sexo == 'M') {
                            sexoCliente.setText(R.string.Male)
                        } else {
                            sexoCliente.setText(R.string.Female)
                        }
                        emailCLiente.setText(response.body()?.email)
                        aniversarioCliente.setText(response.body()?.birthDate)
                        senhaCliente.setText(response.body()?.senha)
                        //ftCliente.setImageURI(response.body()?.foto)
                        val endereco = response.body()?.enderecos
                        //val phonesCliente = response.body()?.cellphone



                    } else {
                        Toast.makeText(this@ProfileActivity,"Erro ao buscar cliente com ID informado. ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Client>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity,"Erro ao conectar-se com o DB. ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("Erro_busca_cliente", "Erro ao tentar recuperar dados do cliente. ${t.message}")
                }
            })
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

    fun onSaveButtonClicked(view: View) {

    }

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

    // Fecha o Drawer ao pressionar o botão de voltar
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}