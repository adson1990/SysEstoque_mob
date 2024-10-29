package com.example.sysestoque

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileActivity : AppCompatActivity() {

    // Variáveis
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
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var radioGroup: RadioGroup

    private var activeColor = "red"
    private var originalClientData: Client? = null
    private var senhaCliente: String? = null
    private var isUpdating = false
    private var isInitializing = false
    private var idCliente: Long = 0L
    private var loginInfo: LoginInfo? = null
    private var novaFotoUri: Uri? = null
    private var updatedPhoto: String? = null
    private var idParametro: Long = 0L

    //Constantes
    private val REQUEST_IMAGE_CAPTURE = 1
    companion object {
        private const val REQUEST_CODE = 1
    }

    /*private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0*/

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityManager.addActivity(this)
        // Instanciando objetos
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
        textView = binding.textView
        seekBarRed = binding.seekBarRed
        seekBarGreen = binding.seekBarGreen
        seekBarBlue = binding.seekBarBlue
        radioGroup = binding.radioGroup

        // persistencia de dados
        authRepository = AuthRepository()
        clientRepository = ClientRepository()
        dbHelperLogin = DbHelperLogin(this)
        dbHelper = ColorDatabaseHelper(this)

        // menu lateral na tela
        val toolbar = findViewById<Toolbar>(R.id.toolbarProfile)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout_profile)
        navigationView = findViewById(R.id.navigation_view_profile)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // buscando ID do cliente da intent
        idParametro = intent.getLongExtra("ID_CLIENTE", -1L)

        // Lidando com cliques no menu
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    funcoes.exibirToast(this@ProfileActivity, R.string.descartar, "", 0)
                    finish()
                }

                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_settings -> {
                    abrirSettingsActivity(idParametro)
                    funcoes.exibirToast(this@ProfileActivity, R.string.descartar, "", 0)
                    finish()
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

                if (!s.toString().startsWith(getString(R.string.sifrao))) {
                    isUpdating = true
                    salarioCliente.setText(getString(R.string.sifrao) + " ")
                    salarioCliente.setSelection(salarioCliente.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isUpdating = false
            }
        })

        /*Calendário retrocedia mês a mês não podendo escolher ano
       o que causava uma experiência ruim em escolher datas muito passadas
       val calendarView: CalendarView = findViewById(R.id.calendarView)
       calendarView.visibility = View.GONE*/
        //CALENDÁRIO RETIRADO DEVIDO FALTA DE USABILIDADE EM RETROCEDER DATAS
        /*calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            btnToggleCalendar.text = selectedDate
            calendarView.visibility = View.GONE
        }*/

        // Data de nascimento do cliente
        val btnToggleCalendar: Button = findViewById(R.id.btnToggleCalendar)
        val calendar = Calendar.getInstance()
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
        // Alteração da cor do nome

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

        // bucando dados do cliente
        loginInfo = dbHelperLogin.getUsuarioLogado(idParametro)!!
        idCliente = loginInfo?.idClient ?: 0L
        var email = loginInfo?.email ?: ""
        val nome = email.substringBefore("@")

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
        textView.text = nome

        getAccessToken(idCliente,email)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                novaFotoUri = getImageUriFromBitmap(imageBitmap)
                ftCliente.setImageURI(novaFotoUri)
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                novaFotoUri = result.data?.data
                ftCliente.setImageURI(novaFotoUri)
            }
        }

        ftCliente.setOnClickListener {
            showImagePickerDialog()
        }

        // Fecha o Drawer ao pressionar o botão de voltar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Se o drawer não estiver aberto, invoca o comportamento padrão
                    isEnabled = false  // Desativa para permitir o comportamento padrão
                    onBackPressedDispatcher.onBackPressed()  // Chama a ação padrão
                }
            }
        })

        // fim do onCreate
    }

    private fun getAccessToken(idCliente: Long, email: String){
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

                        val fotoUser = cliente?.foto?.takeIf { it.isNotEmpty() }
                        if (!fotoUser.isNullOrEmpty()) {
                            val bitmap = base64ToBitmap(fotoUser)
                            if (bitmap != null) {
                                ftCliente.setImageBitmap(bitmap)
                            }
                        }else {
                            val dbhelperLogin = dbHelperLogin.getUsuarioLogado(idParametro)
                            val photoUser = dbhelperLogin?.foto ?: ""
                            if (photoUser.isNotEmpty()) {
                                Glide.with(this@ProfileActivity)
                                    .load(photoUser)
                                    .into(ftCliente)
                            } else {
                                ftCliente.setImageResource(R.mipmap.user_icon) // imagem padrão caso não tenha nem no DB online nem no Android
                            }
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

                        val celulares = cliente?.cellphones ?: emptyList()
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

        val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val localDate = LocalDate.parse(findViewById<Button>(R.id.btnToggleCalendar).text.toString().trim(),dateFormatter)
        val zonedDateTime =
            localDate.atStartOfDay(ZoneId.systemDefault())
        val datNas = Date.from(zonedDateTime.toInstant())
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault())
        val formattedDate = dateFormatOutput.format(datNas)

        // foto em capo TEXT no DB, formato BASE64
        updatedPhoto = originalClientData?.foto ?: ""

        if (novaFotoUri != null) {
            salvarImagem(novaFotoUri!!)
        }
        if(originalClientData!!.email != emailCLiente.text.toString()){
            dbHelperLogin.atualizaUsuarioLogado(emailCLiente.text.toString())
        }

        // salvar imagem em campo BLOB
       /* var byteArray: ByteArray? = null
        if (novaFotoUri != null) {
            val inputStream = contentResolver.openInputStream(novaFotoUri!!)
            byteArray = inputStream?.readBytes() // Lê os bytes da imagem
            inputStream?.close()
        }*/

        val incomeString = salarioCliente.text.toString().replace("R$", "").trim()
        // salvando os dados atuais do cliente
        val updatedClient = Client(
            name = nomeCompleto.text.toString(),
            cpf = cpfClient.text.toString(),
            income = incomeString.toDoubleOrNull() ?: 0.0,
            birthDate = formattedDate,
            sexo = if (sexoCliente.text.toString() == getString(R.string.Male)) 'M' else 'F',
            email = emailCLiente.text.toString(),
            senha = senhaCliente.toString(),
            //foto = byteArray ?: ByteArray(0), salvar em campo BLOB
            foto = updatedPhoto!!,
            cellphones = listOfNotNull(
                if (isPhoneNumberValid(dddCiente1.text.toString().toIntOrNull() ?: 0, celularCliente1.text.toString())) {
                    Cellphone(dddCiente1.text.toString().toInt(), celularCliente1.text.toString(), tpNumero1)
                } else null,
                if (isPhoneNumberValid(dddCiente2.text.toString().toIntOrNull() ?: 0, celularCliente2.text.toString())) {
                    Cellphone(dddCiente2.text.toString().toInt(), celularCliente2.text.toString(), tpNumero2)
                } else null,
                if (isPhoneNumberValid(dddCiente3.text.toString().toIntOrNull() ?: 0, celularCliente3.text.toString())) {
                    Cellphone(dddCiente3.text.toString().toInt(), celularCliente3.text.toString(), tpNumero3)
                } else null
            ),
            enderecos = listOf(
                Enderecos(
                    rua = enderecoCliente.text.toString(),
                    bairro = bairroCliente.text.toString(),
                    num = numCasaCliente.text.toString().toIntOrNull() ?: 0,
                    cidade = cidadeCliente.text.toString(),
                    estado = estadoCliente.text.toString(),
                    country = paisCliente,
                    cep = cepCliente.text.toString()
                )
            )
        )
        // Compara os dados atuais com os originais
        if (updatedClient != originalClientData) {

            // Se houver mudanças, envie os dados para o endpoint
            clientRepository.updateClient(originalClientData!!.email, loginInfo!!.idClient, updatedClient, object : Callback<Client> {
                override fun onResponse(call: Call<Client>, response: Response<Client>) {
                    if (response.isSuccessful) {
                        println("Cliente atualizado com sucesso")
                        progressBar.visibility = View.GONE
                        finish()
                    } else {
                        println("Erro ao atualizar cliente: ${response.errorBody()?.string()}")
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Client>?, t: Throwable) {
                    println("Erro na requisição: ${t.message}")
                    progressBar.visibility = View.GONE
                }
            })
        } else {
            // Se não houver mudanças, apenas encerre a activity
            finish()
        }
    }

    private fun isPhoneNumberValid(ddd: Int, number: String): Boolean {
        return ddd > 9 && number.isNotBlank()
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

    private fun showImagePickerDialog() {
        val options = arrayOf("Câmera", "Galeria")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha uma opção")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkPermissionsAndOpenCamera() // Chama o método de verificação de permissões
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(takePictureIntent) // Chama o launcher da câmera
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickPhotoIntent)
    }

    private fun checkPermissionsAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        } else {
            openCamera()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val tempFile = File(cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
        tempFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return FileProvider.getUriForFile(this, "${packageName}.provider", tempFile)
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        // Remove o prefixo "data:image/png;base64," se necessário
        val cleanBase64 = base64String.replace("data:image/png;base64,", "")
        val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun salvarImagemNaMemoriaInterna(bitmap: Bitmap, context: Context): String? {
        val nomeArquivo = "imagem_cliente_${System.currentTimeMillis()}.jpg"

        // Diretório de arquivos internos do app
        val diretorio = context.filesDir
        val arquivoImagem = File(diretorio, nomeArquivo)

        try {
            // Salvar a imagem no formato JPEG
            val outputStream = FileOutputStream(arquivoImagem)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Retornar o caminho completo do arquivo salvo
            return arquivoImagem.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun abrirSettingsActivity(id: Long) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("ID_CLIENTE", id)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // A permissão foi concedida, chama o método para salvar a imagem
                    if (novaFotoUri != null) {
                        salvarImagem(novaFotoUri!!)
                    }
                } else {
                    // A permissão foi negada, exibe uma mensagem ao usuário
                    funcoes.exibirToast(this@ProfileActivity,R.string.denied_permition,"", 2)
                }
            }
        }
    }

    private fun salvarImagem(novaFotoUri: Uri) {
        // A imagem foi atualizada, então converte para Base64
        val inputStream = contentResolver.openInputStream(novaFotoUri)
        val byteArray = inputStream?.readBytes()
        inputStream?.close()

        if (byteArray != null) {
            updatedPhoto = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Se o caminho foi gerado, convertemos em bitmap para salvar no DB do Android
            if (!updatedPhoto.isNullOrEmpty()) {
                val bitmap = base64ToBitmap(updatedPhoto!!)
                val caminhoImagem = salvarImagemNaMemoriaInterna(bitmap!!, this@ProfileActivity)
                dbHelperLogin.salvarCaminhoFoto(caminhoImagem.toString())
            } else {
                Log.e("ProfileActivity", "Caminho da imagem não gerado.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeActivity(this)
    }
}