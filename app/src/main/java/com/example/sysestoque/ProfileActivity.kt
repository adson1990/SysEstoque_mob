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
import com.bumptech.glide.Glide
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.Cellphone
import com.example.sysestoque.backend.Client
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.Enderecos
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
    private lateinit var ftCliente: ImageView
    private lateinit var nomeCompleto: EditText
    private lateinit var cpfClient: EditText
    private lateinit var emailCLiente: EditText
    private lateinit var senhaCliente: EditText
    private lateinit var sexoCliente: TextView
    private lateinit var aniversarioCliente: Button
    private lateinit var salarioCliente: EditText
    private lateinit var enderecoCliente: EditText
    private lateinit var bairroCliente: EditText
    private lateinit var numCasaCliente: EditText
    private lateinit var cepCliente: EditText
    private lateinit var cidadeCliente: EditText
    private lateinit var estadoCliente: EditText
    private lateinit var dddCiente1: EditText
    private lateinit var dddCiente2: EditText
    private lateinit var dddCiente3: EditText
    private lateinit var celularCliente1: EditText
    private lateinit var celularCliente2: EditText
    private lateinit var celularCliente3: EditText
    private lateinit var dbHelper: ColorDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var authRepository: AuthRepository
    private lateinit var clientRepository : ClientRepository

    private var activeColor = "red"
    private var originalClientData: Client? = null

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
        celularCliente1 = findViewById(R.id.edtNumeroCel1)
        celularCliente2 = findViewById(R.id.edtNumeroCel2)
        celularCliente3 = findViewById(R.id.edtNumeroCel3)

        dddCiente1 = findViewById(R.id.edtDDD1)
        dddCiente2 = findViewById(R.id.edtDDD2)
        dddCiente3 = findViewById(R.id.edtDDD3)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository()
        clientRepository = ClientRepository()

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
                    Toast.makeText(this, "Dados descartados!", Toast.LENGTH_SHORT).show()
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

        val btnToggleCalendar: Button = findViewById(R.id.btnToggleCalendar)
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

        fun getDadosCliente(idCliente: Long, token: String) {
            clientRepository.getClientById(idCliente, token, object : Callback<Client> {
                override fun onResponse(call: Call<Client>, response: Response<Client>) {
                    if (response.isSuccessful) {
                        val cliente = response.body()
                        originalClientData = cliente

                        nomeCompleto.setText(cliente?.name)
                        cpfClient.setText(cliente?.cpf)
                        salarioCliente.setText(cliente?.income.toString())
                        if (cliente?.sexo == 'M') {
                            sexoCliente.setText(R.string.Male)
                        } else {
                            sexoCliente.setText(R.string.Female)
                        }
                        emailCLiente.setText(cliente?.email)
                        aniversarioCliente.text = cliente?.birthDate
                        senhaCliente.setText(cliente?.senha)

                        cliente?.foto?.let {
                            Glide.with(this@ProfileActivity)
                                .load(it)
                                .into(ftCliente)
                        }

                        val endereco = cliente?.enderecos?.firstOrNull()
                        endereco?.let {
                            enderecoCliente.setText(it.rua)
                            bairroCliente.setText(it.bairro)
                            numCasaCliente.setText(it.num.toString())
                            cepCliente.setText(it.cep)
                            cidadeCliente.setText(it.estado)
                            estadoCliente.setText(it.country)
                        }

                        val celulares = cliente?.cellphone

                        celulares?.let {
                            when (celulares.size) {
                                1 -> {
                                    dddCiente1.setText(celulares[0].ddd.toString())
                                    celularCliente1.setText(celulares[0].number)
                                }

                                2 -> {
                                    dddCiente1.setText(celulares[0].ddd.toString())
                                    celularCliente1.setText(celulares[0].number)

                                    dddCiente2.setText(celulares[1].ddd.toString())
                                    celularCliente2.setText(celulares[1].number)
                                }

                                3 -> {
                                    dddCiente1.setText(celulares[0].ddd.toString())
                                    celularCliente1.setText(celulares[0].number)

                                    dddCiente2.setText(celulares[1].ddd.toString())
                                    celularCliente2.setText(celulares[1].number)

                                    dddCiente3.setText(celulares[2].ddd.toString())
                                    celularCliente3.setText(celulares[2].number)
                                }
                            }
                        }

                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Erro ao buscar cliente com ID informado. ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Client>, t: Throwable) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Erro ao conectar-se com o DB. ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(
                        "Erro_busca_cliente",
                        "Erro ao tentar recuperar dados do cliente. ${t.message}"
                    )
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
        if (red == 0 && green == 0 && blue == 0) {
            textView.setTextColor(Color.WHITE)
        } else {
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

    fun salvaDadosAtuaisCliente(client: Client){

    }

    fun onSaveButtonClicked(view: View) {
        // salvando os dados atuais do cliente
        val updatedClient = Client(
            name = nomeCompleto.text.toString(),
            cpf = cpfClient.text.toString(),
            income = salarioCliente.text.toString().toDoubleOrNull() ?: 0.0,
            birthDate = aniversarioCliente.text.toString(),
            sexo = if (sexoCliente.text.toString() == getString(R.string.Male)) 'M' else 'F',
            email = emailCLiente.text.toString(),
            senha = senhaCliente.text.toString(),
            foto = originalClientData?.foto ?: "", // Mantenha a foto original, caso não tenha mudado
            cellphone = listOf(
                Cellphone(ddd = dddCiente1.text.toString().toIntOrNull() ?: 0, number = celularCliente1.text.toString(), tipo = 'C'),
                Cellphone(ddd = dddCiente2.text.toString().toIntOrNull() ?: 0, number = celularCliente2.text.toString(), tipo = 'C'),
                Cellphone(ddd = dddCiente3.text.toString().toIntOrNull() ?: 0, number = celularCliente3.text.toString(), tipo = 'C')
            ),
            enderecos = listOf(
                Enderecos(
                    rua = enderecoCliente.text.toString(),
                    bairro = bairroCliente.text.toString(),
                    num = numCasaCliente.text.toString().toIntOrNull() ?: 0,
                    estado = estadoCliente.text.toString(),
                    country = cidadeCliente.text.toString(),
                    cep = cepCliente.text.toString()
                )
            )
        )

        // Compara os dados atuais com os originais
        if (updatedClient != originalClientData) {
            // Se houver mudanças, envie os dados para o endpoint
            clientRepository.registerClient(updatedClient, object : Callback<Client> {
                override fun onResponse(call: Call<Client>, response: Response<Client>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Erro ao salvar dados: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Client>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Erro ao conectar-se com o servidor: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            // Se não houver mudanças, apenas encerre a activity
            finish()
        }
    }

    private fun logout() {
        val dbHelperLogin = DbHelperLogin(this)
        dbHelperLogin.lembrarCliente(false)
        Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()
        chamarLoginActivity()
    }

    private fun chamarLoginActivity() {
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