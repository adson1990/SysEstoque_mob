package com.example.sysestoque

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
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
import com.example.sysestoque.backend.TokenResponse
import com.example.sysestoque.data.database.ColorDatabaseHelper
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.data.database.LoginInfo
import com.example.sysestoque.data.utilitarios.ActivityManager
import com.example.sysestoque.data.utilitarios.Funcoes
import com.example.sysestoque.databinding.ActivityProfileBinding
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    private lateinit var sexoCliente: TextView
    private lateinit var aniversarioCliente: Button
    private lateinit var salarioCliente: EditText
    private lateinit var enderecoCliente: EditText
    private lateinit var bairroCliente: EditText
    private lateinit var numCasaCliente: EditText
    private lateinit var cepCliente: EditText
    private lateinit var cidadeCliente: EditText
    private lateinit var estadoCliente: EditText
    private lateinit var paisCliente: Spinner
    private lateinit var dddCiente1: EditText
    private lateinit var dddCiente2: EditText
    private lateinit var dddCiente3: EditText
    private lateinit var tipoNumero1: Spinner
    private lateinit var tipoNumero2: Spinner
    private lateinit var tipoNumero3: Spinner
    private lateinit var celularCliente1: EditText
    private lateinit var celularCliente2: EditText
    private lateinit var celularCliente3: EditText
    private lateinit var dbHelper: ColorDatabaseHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var authRepository: AuthRepository
    private lateinit var clientRepository : ClientRepository
    private lateinit var dbHelperLogin: DbHelperLogin
    private lateinit var funcoes: Funcoes
    private lateinit var progressBar: ProgressBar

    private var activeColor = "red"
    private var originalClientData: Client? = null
    private var senhaCliente: String? = null
    private var isUpdating = false
    private var isInitializing = false
    private var idCliente: Long = 0L
    private var loginInfo: LoginInfo? = null

    /*private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0*/

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityManager.addActivity(this)
        funcoes = Funcoes()
        progressBar = binding.requestSave

        // referências das views na tela
        ftCliente = binding.ftUsuario
        nomeCompleto = binding.edtNomeCompleto
        cpfClient = binding.edtCPF
        emailCLiente = binding.edtEmail
        sexoCliente = binding.textView5
        aniversarioCliente = binding.btnToggleCalendar
        salarioCliente = binding.edtSalario
        enderecoCliente = binding.edtEndereco
        bairroCliente = binding.edtBairro
        numCasaCliente = binding.edtNumCasa
        cepCliente = binding.edtCEP
        cidadeCliente = binding.edtCidade
        estadoCliente = binding.edtEstado
        paisCliente = binding.spinnerCountry
        celularCliente1 = binding.edtNumeroCel1
        celularCliente2 = binding.edtNumeroCel2
        celularCliente3 = binding.edtNumeroCel3
        dddCiente1 = binding.edtDDD1
        dddCiente2 = binding.edtDDD2
        dddCiente3 = binding.edtDDD3
        tipoNumero1 = binding.spinnerTipoNumero1
        tipoNumero2 = binding.spinnerTipoNumero2
        tipoNumero3 = binding.spinnerTipoNumero3

        // persistencia de dados
        authRepository = AuthRepository()
        clientRepository = ClientRepository()
        dbHelperLogin = DbHelperLogin(this)
        dbHelper = ColorDatabaseHelper(this)

        // menu lateral na tela
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout_profile)
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
                    funcoes.exibirToast(this@ProfileActivity, R.string.descartar, "", 0)
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
                    funcoes.logout(this@ProfileActivity)
                    drawerLayout.closeDrawers() // Fecha o drawer após a ação
                    return@setNavigationItemSelectedListener true
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Salário começa com R$
        salarioCliente.setText(getString(R.string.sifrao) + " ")
        salarioCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating || isInitializing) return

                val texto = s?.toString() ?: ""

                if (!texto.startsWith(getString(R.string.sifrao))) {
                    isUpdating = true
                    salarioCliente.setText(getString(R.string.sifrao) + " " + texto.trim())
                    salarioCliente.setSelection(salarioCliente.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isUpdating = false
            }
        })

        // Alteração da cor do nome
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

        val colors = dbHelper.getColors(idCliente)
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

        // Data de nascimento do cliente
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

        // bucando dados do cliente
        loginInfo = dbHelperLogin.getUsuarioLogado()!!
        idCliente = loginInfo?.idClient ?: 0L
        var email = loginInfo?.email ?: ""
        val nome = email.substringBefore("@")

        textView.setText(nome)

        getAccessToken(idCliente,email)

        // fim do onCreate
    }

    fun getAccessToken(idCliente: Long, email: String){
        authRepository.getTokenByEmail(email, object : Callback<TokenResponse>{
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful){
                    val token = response.body()?.accessToken ?: ""
                    if (!token.isNullOrEmpty()) {
                        getDadosCliente(idCliente, token)
                    } else {
                        funcoes.exibirToast(this@ProfileActivity, R.string.erro_buscar_token, "", 1)
                    }
                }else {
                    funcoes.exibirToast(this@ProfileActivity, R.string.erro_buscar_cliente, "", 1)
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                funcoes.exibirToast(this@ProfileActivity, R.string.erro_conexao_db, t.message.toString(), 1)
                Log.e("Erro_busca_cliente", "Erro ao tentar recuperar token: ${t.message}")
            }

        })
    }

    fun getDadosCliente(idCliente: Long, token: String) {
        clientRepository.getClientById(idCliente, token, object : Callback<Client> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    val cliente = response.body()
                    if (cliente != null) {
                        originalClientData = cliente

                        nomeCompleto.setText(cliente?.name)
                        cpfClient.setText(cliente?.cpf)
                        isInitializing = true
                        salarioCliente.setText(getString(R.string.sifrao) + " " + cliente.income.toString())
                        isInitializing = false
                        if (cliente?.sexo == 'M') {
                            sexoCliente.setText(R.string.Male)
                        } else {
                            sexoCliente.setText(R.string.Female)
                        }
                        emailCLiente.setText(cliente?.email)
                        senhaCliente = cliente?.senha.toString()
                        val dataCliente = formatarDataParaBrasileiro(cliente?.birthDate.toString())
                        aniversarioCliente.text = dataCliente

                        cliente?.foto?.let {
                            Glide.with(this@ProfileActivity)
                                .load(it)
                                .into(ftCliente)
                        }

                        val endereco = cliente?.enderecos?.firstOrNull()
                        val countryFromDB = endereco?.country ?: ""
                        val countriesArray = resources.getStringArray(R.array.countries_array)
                        val countryIndex = countriesArray.indexOf(countryFromDB)
                        if (countryIndex != -1) {
                            paisCliente.setSelection(countryIndex)
                        }
                        endereco?.let {
                            enderecoCliente.setText(it.rua)
                            bairroCliente.setText(it.bairro)
                            numCasaCliente.setText(it.num.toString())
                            cepCliente.setText(it.cep)
                            cidadeCliente.setText(it.cidade) // verificar o por quê de a cidade não carregar
                            estadoCliente.setText(it.estado)
                        }

                        val celulares = cliente?.cellphone ?: emptyList()
                        preencherCamposDeTelefone(celulares)
                    }
                } else {
                    funcoes.exibirToast(this@ProfileActivity, R.string.erro_buscar_cliente,"",1)
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                funcoes.exibirToast(this@ProfileActivity, R.string.erro_conexao_db, t.message.toString(), 1)
                Log.e(
                    "Erro_busca_cliente",
                    "Erro ao tentar recuperar dados do cliente. ${t.message}"
                )
            }
        })
    }

    private fun preencherCamposDeTelefone(celulares: List<Cellphone>) {
        // Array com os IDs dos EditTexts e Spinners
        val dddFields = listOf(
            dddCiente1,
            dddCiente2,
            dddCiente3
        )

        val numeroFields = listOf(
            celularCliente1,
            celularCliente2,
            celularCliente3
        )

        val tipoSpinners = listOf(
            tipoNumero1,
            tipoNumero2,
            tipoNumero3
        )

        val tiposArray = resources.getStringArray(R.array.tipo_telefone)

        // Preencher cada campo com base na lista de celulares recuperada
        celulares.forEachIndexed { index, phoneData ->
            if (index < 3) {
                dddFields[index].setText(phoneData.ddd.toString())
                numeroFields[index].setText(phoneData.number)

                // Encontrar o índice correspondente ao tipo de telefone no Spinner
                val tipoIndex = getTipoIndex(phoneData.tipo.toString(), tiposArray)

                if (tipoIndex != -1) {
                    tipoSpinners[index].setSelection(tipoIndex)
                }
            }
        }
    }

    private fun getTipoIndex(codigo: String, tiposArray: Array<String>): Int {
        val tipoString = when (codigo) {
            "P" -> getString(R.string.num_pessoal)
            "T" -> getString(R.string.num_profissional)
            "R" -> getString(R.string.num_residencial)
            "C" -> getString(R.string.num_comercial)
            else -> ""
        }
        return tiposArray.indexOf(tipoString)
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

        loginInfo?.let {
            dbHelper.saveColors(red, green, blue, it.idClient)
        }
        dbHelper.saveColors(red, green, blue, loginInfo?.idClient ?: 0L) // salvando no DB do android as cores
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSaveButtonClicked(view: View) {
        progressBar.visibility = View.VISIBLE

        val tpNumero1 = tipoNumero1.selectedItem.toString().firstOrNull() ?: 'P' // Default: 'P' para Pessoal
        val tpNumero2 = tipoNumero2.selectedItem.toString().firstOrNull() ?: 'P'
        val tpNumero3 = tipoNumero3.selectedItem.toString().firstOrNull() ?: 'P'
        val paisCliente = paisCliente.selectedItem.toString() ?: "Brasil"

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val localDate = LocalDate.parse(findViewById<Button>(R.id.btnToggleCalendar).text.toString(),dateFormatter)
        val zonedDateTime =
            localDate.atStartOfDay(ZoneId.systemDefault())
        val datNas = Date.from(zonedDateTime.toInstant())
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault())
        val formattedDate = dateFormatOutput.format(datNas)

        // salvando os dados atuais do cliente
        val updatedClient = Client(
            name = nomeCompleto.text.toString(),
            cpf = cpfClient.text.toString(),
            income = salarioCliente.text.toString().toDoubleOrNull() ?: 0.0,
            birthDate = formattedDate,
            sexo = if (sexoCliente.text.toString() == getString(R.string.Male)) 'M' else 'F',
            email = emailCLiente.text.toString(),
            senha = senhaCliente.toString(),
            foto = originalClientData?.foto ?: "", // Mantenha a foto original, caso não tenha mudado
            cellphone = listOf(
                Cellphone(ddd = dddCiente1.text.toString().toIntOrNull() ?: 0, number = celularCliente1.text.toString(), tipo = tpNumero1),
                Cellphone(ddd = dddCiente2.text.toString().toIntOrNull() ?: 0, number = celularCliente2.text.toString(), tipo = tpNumero2),
                Cellphone(ddd = dddCiente3.text.toString().toIntOrNull() ?: 0, number = celularCliente3.text.toString(), tipo = tpNumero3)
            ),
            enderecos = listOf(
                Enderecos(
                    rua = enderecoCliente.text.toString(),
                    bairro = bairroCliente.text.toString(),
                    num = numCasaCliente.text.toString().toIntOrNull() ?: 0,
                    cidade = cidadeCliente.toString(),
                    estado = estadoCliente.text.toString(),
                    country = paisCliente,
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
                        funcoes.exibirToast(this@ProfileActivity, R.string.salvar_dados_ok, "",0)
                        progressBar.visibility = View.GONE
                        finish()
                    } else {
                        funcoes.exibirToast(this@ProfileActivity, R.string.erro_salvar_dados, response.message(),1)
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Client>, t: Throwable) {
                    funcoes.exibirToast(this@ProfileActivity,R.string.erro_conexao_db, t.message.toString(),1)
                    progressBar.visibility = View.GONE
                }
            })
        } else {
            // Se não houver mudanças, apenas encerre a activity
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatarDataParaBrasileiro(dataISO: String): String {
        // Parser para ler o formato ISO 8601 com time zone
        val formatoISO = DateTimeFormatter.ISO_ZONED_DATE_TIME

        // Formato brasileiro (dd/MM/yyyy)
        val formatoBrasileiro = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

        // Faz a conversão da string para ZonedDateTime
        val data = ZonedDateTime.parse(dataISO, formatoISO)

        // Converte para o formato brasileiro e retorna
        return data.format(formatoBrasileiro)
    }

    // Fecha o Drawer ao pressionar o botão de voltar
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeActivity(this)
    }
}