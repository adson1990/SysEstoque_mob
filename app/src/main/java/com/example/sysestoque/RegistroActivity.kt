package com.example.sysestoque

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sysestoque.backend.Cellphone
import com.example.sysestoque.backend.Client
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.Enderecos
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class RegistroActivity : AppCompatActivity() {

    //VARIÁVEIS
    private lateinit var clientRepository: ClientRepository
    private lateinit var progressBar: ProgressBar
    private val delayMillis: Long = 5000

    private lateinit var edtCPF: EditText
    private lateinit var edtSenha: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var linearLayoutPhoneNumbers: LinearLayout
    private lateinit var btnAddPhone: ImageButton
    private lateinit var spinnerCelphone: Spinner
    private var phoneNumberCount = 1

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.registro_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_cadastro)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Referências e validações dos campos

        // Nome com letras maiúsculas
        //-----------------------------------------------------------------------------------------------------------
        val edtNome = findViewById<EditText>(R.id.edtNomeCompleto)
        edtNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                edtNome.removeTextChangedListener(this) // Previne loops infinitos
                edtNome.setText(s.toString().uppercase())
                edtNome.setSelection(s?.length ?: 0) // Posiciona o cursor no final
                edtNome.addTextChangedListener(this)
            }
        })

        // validação do e-mail digitado
        //-----------------------------------------------------------------------------------------------------------
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtEmail.removeTextChangedListener(this) // remove o listener temporariamente para evitar loop
                val emailInput = s.toString()

                // regex para verificar a terminação do e-mail após o "@"
                val emailPattern = Regex(".+@.+\\.com.*")

                if ( android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
                    && emailPattern.containsMatchIn(emailInput) ) {
                    edtEmail.setTextColor(
                        ContextCompat.getColor(this@RegistroActivity,R.color.green_600)
                    )
                } else {
                    // edtEmail.setTextColor(resources.getColor(R.color.red_700, theme)) Mais moderno porém requer API(M)
                    edtEmail.setTextColor(
                        ContextCompat.getColor(this@RegistroActivity,R.color.red_700)
                    )
                }

                edtEmail.setSelection(emailInput?.length ?: 0)
                edtEmail.addTextChangedListener(this) // Reatribui o listener
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Máscara e validação do CPF
        //-----------------------------------------------------------------------------------------------------------
        edtCPF = findViewById<EditText>(R.id.edtCPF)
        edtCPF.addCpfMask()
        edtCPF.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateCpf(edtCPF)
                }
            }
        // Listener para bloquear o botão "Avançar" do teclado virtual se CPF for inválido
        edtCPF.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (!validateCpf(edtCPF)) {
                    edtCPF.requestFocus()
                    true // Bloqueia o avanço
                } else {
                    false // Permite o avanço se o CPF for válido
                }
            } else {
                false
            }
        }

        //Validação da composição da senha
        //-----------------------------------------------------------------------------------------------------------
        edtSenha = findViewById(R.id.edtSenha)
        edtSenha.validPassword()
        edtSenha.setOnTouchListener { _, event ->
            // Verificar primeiro o botão de olho para revelar/ocultar senha
            if (event.action == MotionEvent.ACTION_UP) {
                // Verificar se o toque está na posição do drawable no final (o olho)
                //índice 1 = start,índice 2 = end, índice 3 = top e índice 4 = bottom
                if (event.rawX >= edtSenha.right - edtSenha.compoundDrawables[2].bounds.width()) {
                    if (edtSenha.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        edtSenha.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edtSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                    } else {
                        edtSenha.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        edtSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                    }
                    edtSenha.setSelection(edtSenha.text.length)
                    return@setOnTouchListener true
                }
            }

            // Agora, verificar a validação do CPF
            if (!validateCpf(edtCPF)) {
                edtCPF.requestFocus() // Manter o foco no campo CPF se for inválido
                return@setOnTouchListener true // Bloquear o toque no próximo campo
            }

            false // Permitir o toque no próximo campo se o CPF for válido
        }

        // Campo salário começa obrigatoriamente com R$
        //-----------------------------------------------------------------------------------------------------------
        val edtSalario = findViewById<EditText>(R.id.edtSalario)
        edtSalario.setText(R.string.sifrao)
        edtSalario.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                if (!s.toString().startsWith(getString(R.string.sifrao))) {
                    isUpdating = true
                    edtSalario.setText(getString(R.string.sifrao))
                    edtSalario.setSelection(edtSalario.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                isUpdating = false
            }
        })

        // Data de nascimento com máscara
        // Na Activity de edição dos dados do cliente a data de nascimento será um DatePickerDialog
        //-----------------------------------------------------------------------------------------------------------
        val nascimento = findViewById<EditText>(R.id.edtNascimento)
        nascimento.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isUpdating) return

                nascimento.removeTextChangedListener(this)

                var text = p0.toString().replace(Regex("[^\\d]"),"") // Remove qualquer caracter que não seja número, mesmo o campo contendo inputType="date"
                val length = text.length

                if (length > 2) {
                    text = text.substring(0, 2) + "/" + text.substring(2)
                }
                if (length > 4) {
                    text = text.substring(0, 5) + "/" + text.substring(5)
                }

                isUpdating = true // previne loops infinitos ou reentrada no método onTextChanged.
                nascimento.setText(text)
                nascimento.setSelection(text.length)
                isUpdating = false

                nascimento.addTextChangedListener(this)
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        //opção de países do endereço
        //-----------------------------------------------------------------------------------------------------------
        spinnerCountry = findViewById(R.id.spinnerCountry)

        val countriesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        )

        countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = countriesAdapter

        // Telefone
        //Adicionando mais números de telefone
        // -----------------------------------------------------------------------------------------------------------
        linearLayoutPhoneNumbers = findViewById(R.id.linearLayoutPhoneNumbers)
        btnAddPhone = findViewById(R.id.addPhone)
        spinnerCelphone = findViewById(R.id.spinnerTipoNumero)

        val celphoneAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_telefone,
            android.R.layout.simple_spinner_item
        )
        celphoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCelphone.adapter = celphoneAdapter

        btnAddPhone.setOnClickListener {
            if (phoneNumberCount < 3) { // Permite até 3 números de contato
                addPhoneNumberFields()
                phoneNumberCount++
            } else {
                btnAddPhone.isEnabled = false // Desativa o botão quando atingir o limite
            }
        }

        clientRepository = ClientRepository()
        progressBar = findViewById(R.id.requestLogin)

        val btnRegistrar: Button = findViewById(R.id.btnCadastrar)
        btnRegistrar.setOnClickListener {
            onRegisterButtonClicked()
        }

        // fim oncreate
    }

    private fun EditText.validPassword() {
        this.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val inputSenha = this.text.toString()
                val passwordRegex =
                    """^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&+-.;></)(])[A-Za-z\d@$!%*?&+-.;></)(]{6,}$"""
                val senhaPattern = Regex(passwordRegex)

                if (!senhaPattern.matches(inputSenha)) {
                    val builder = AlertDialog.Builder(this@RegistroActivity)
                    builder.setTitle(R.string.error)
                    builder.setMessage(R.string.alert_password)
                    builder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        this.requestFocus()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }
    }

    fun EditText.addCpfMask() {
        var isValidating = false  // Flag para evitar validação repetida
        this.addTextChangedListener(object : TextWatcher {
            private var isUpdating: Boolean = false //flag para verificar se o TextWatcher foi acionado
            private var oldText: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(".", "").replace("-", "")

                if (count == 0) // Se a quantidade diminuiu, é porque foi uma deleção
                    isUpdating = true

                if (isUpdating) {
                    oldText = str
                    isUpdating = false
                    return
                }

                var formattedText = ""
                var i = 0
                while (i < str.length && i < 11) {
                    formattedText += str[i]
                    if ((i == 2 || i == 5) && i + 1 < str.length) {
                        formattedText += "."
                    } else if (i == 8 && i + 1 < str.length) {
                        formattedText += "-"
                    }
                    i++
                }

                isUpdating = true
                this@addCpfMask.setText(formattedText)
                this@addCpfMask.setSelection(formattedText.length)
            }

            // Valida de CPF está correto
            override fun afterTextChanged(s: Editable?) {
                edtCPF.removeTextChangedListener(this)
                val cpf = s.toString().replace(".", "").replace("-", "")
                if (cpf.length == 11) {
                    if (isCPF(cpf)) {
                        if (isValidating) return
                        Toast.makeText(this@RegistroActivity, "CPF válido", Toast.LENGTH_SHORT)
                            .show()
                        isValidating = true // impede que o toast seja exibido 2x
                    } else {
                        isValidating = false
                        Toast.makeText(this@RegistroActivity, "CPF inválido", Toast.LENGTH_LONG)
                            .show()
                        edtCPF.requestFocus()
                    }
                }
                edtCPF.addTextChangedListener(this)
            }
        })
    }

    fun isCPF(cPF: String): Boolean {
        Log.i("ValidaçãoCPF", "Início da validação do CPF: $cPF")

        if (cPF == "00000000000" ||
            cPF == "11111111111" ||
            cPF == "22222222222" ||
            cPF == "33333333333" ||
            cPF == "44444444444" ||
            cPF == "55555555555" ||
            cPF == "66666666666" ||
            cPF == "77777777777" ||
            cPF == "88888888888" ||
            cPF == "99999999999" ||
            (cPF.length != 11)
        ) {
            Log.e("ValidaçãoCPF", "CPF inválido: $cPF")
            return (false)
        }

        try {
            val numbers = cPF.map { it.toString().toInt() }

            val firstNineDigits = numbers.subList(0, 9)
            val firstCheckerDigit = calculateDigit(firstNineDigits, 10)

            val firstTenDigits = numbers.subList(0, 10)
            val secondCheckerDigit = calculateDigit(firstTenDigits, 11)

            return firstCheckerDigit == numbers[9] && secondCheckerDigit == numbers[10]
        } catch (e: Exception) {
            Log.e("ValidaçãoCPF", "Erro ao tentar validar CPF: $cPF")
            return false
        }
    }

    private fun calculateDigit(digits: List<Int>, factor: Int): Int {
        var sum = 0
        var weight = factor

        for (digit in digits) {
            sum += digit * weight
            weight--
        }

        val result = 11 - (sum % 11)
        return if (result >= 10) 0 else result
    }

    fun validateCpf(edtCPF: EditText): Boolean {
        val cpf = edtCPF.text.toString().replace(".", "").replace("-", "")
        return if (cpf.length == 11 && isCPF(cpf)) {
            true // CPF é válido
        } else {
            Toast.makeText(this@RegistroActivity, "CPF inválido", Toast.LENGTH_LONG).show()
            false // CPF inválido
        }
    }

    private fun addPhoneNumberFields() {
        val newLinearLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // Cria o EditText para o DDD
        val dddEditText = EditText(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
            hint = "DDD"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Cria o EditText para o número de telefone
        val phoneNumberEditText = EditText(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.6f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
            hint = getString(R.string.number)
            inputType = InputType.TYPE_CLASS_PHONE
        }

        // Cria o Spinner para o tipo de número
        val tipoPhoneNumberSpinner = Spinner(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
                marginEnd = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
        }
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_telefone,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoPhoneNumberSpinner.adapter = adapter

        newLinearLayout.addView(dddEditText)
        newLinearLayout.addView(phoneNumberEditText)
        newLinearLayout.addView(tipoPhoneNumberSpinner)

        linearLayoutPhoneNumbers.addView(newLinearLayout)
        Log.i("AddNewPhone", "Novo número adicionado")
    }

    // Função para recuperar os números de telefone ao enviar o formulário
    fun getPhoneNumbers(): MutableList<Cellphone> {
        val phoneNumbers = mutableListOf<Cellphone>()

        for (i in 0 until linearLayoutPhoneNumbers.childCount) {
            val phoneLayout = linearLayoutPhoneNumbers.getChildAt(i) as? LinearLayout

            if (phoneLayout != null) {
                // Recupera os campos DDD, Telefone e Spinner
                val edtDDD = phoneLayout.getChildAt(0) as EditText
                val edtPhone = phoneLayout.getChildAt(1) as EditText
                val spinnerType = phoneLayout.getChildAt(2) as Spinner

                // Converte o DDD para Int, telefone como String, e tipo como Char
                val ddd = edtDDD.text.toString().toIntOrNull() ?: 0
                val number = edtPhone.text.toString()
                val tipo = spinnerType.selectedItem.toString().firstOrNull() ?: ' '

                // Adiciona à lista de números de telefone
                phoneNumbers.add(Cellphone(ddd, number, tipo))
            }
        }
        return phoneNumbers
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onRegisterButtonClicked() {
        progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            Log.i("Cadastro-1", "Início do processo de cadastro de cliente.")
            registerClient()
        }, delayMillis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerClient() {
        val nome = findViewById<EditText>(R.id.edtNomeCompleto).text.toString()
        val email = findViewById<EditText>(R.id.edtEmail).text.toString()
        val cpf = findViewById<EditText>(R.id.edtCPF).text.toString()
        val senha = findViewById<EditText>(R.id.edtSenha).text.toString()

        /* As próx linhas resolvem um problema de conversão da data em Json que na passagem de informações
        * o Json estava com o campo de data vazio devido a uma má conversão de formato. */
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Como desejo formatar a data
        val localDate = LocalDate.parse(findViewById<EditText>(R.id.edtNascimento).text.toString(),
                                        dateFormatter) //linkando o campo formatando com meu dateFormatter
        val zonedDateTime =
            localDate.atStartOfDay(ZoneId.systemDefault()) //retornar o LocalDate como ZoneDateTime com hora 00:00, systemDefault fuso padrão de onde o sistema está rodando
        val datNas = Date.from(zonedDateTime.toInstant()) //convertendo o resultado obtido em tipo Instant
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault()) //variável que irá formatar a saída da data no modelo que o BD aceita
        val formattedDate = dateFormatOutput.format(datNas) // Data sendo formatada

        val rua = findViewById<EditText>(R.id.edtEndereco).text.toString()
        val bairro = findViewById<EditText>(R.id.edtBairro).text.toString()
        val numCasa = Integer.parseInt(findViewById<EditText>(R.id.edtNumCasa).text.toString())
        val cep = findViewById<EditText>(R.id.edtCEP).text.toString()
        val estado = findViewById<EditText>(R.id.edtEstado).text.toString()

        val renda = findViewById<EditText>(R.id.edtSalario).text.toString()
        val rendaLimpa = renda.replace("R$", "").replace(",", "").trim()
        val income: Double = rendaLimpa.toDoubleOrNull() ?: 0.0

        val sexoMasc = findViewById<RadioButton>(R.id.rbMasculino)
        val spinnerCountry = findViewById<Spinner>(R.id.spinnerCountry)
        val country = spinnerCountry.selectedItem.toString()

        val phoneNumberDataList = getPhoneNumbers()
        val cellphones = mutableListOf<Cellphone>()
        for (phoneData in phoneNumberDataList) {
            cellphones.add(
                Cellphone(
                    ddd = phoneData.ddd,
                    number = phoneData.number,
                    tipo = phoneData.tipo
                )
            )
        }

        val enderecos = mutableListOf<Enderecos>()
        enderecos.add(
            Enderecos(
                rua = rua,
                bairro = bairro,
                num = numCasa,
                estado = estado,
                country = country,
                cep = cep
            )
        )

        val sex = if (sexoMasc.isChecked) 'M' else 'F'

        val gson = Gson()

        val client = Client(
            name = nome,
            cpf = cpf,
            income = income,
            birthDate = formattedDate,
            sexo = sex,
            email = email,
            senha = senha,
            cellphone = cellphones,
            enderecos = enderecos
        )

        // Converter o cliente para JSON
        val json = gson.toJson(client)

        // Log do objeto cliente
        Log.d("Client", "Client: $client")
        Log.d("Client JSON", "Client JSON: $json") // Verificar o JSON gerado

        clientRepository.registerClient(client, object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Log.i("Cadastrado", "Cliente cadastrado com sucesso.")
                    Toast.makeText(
                        this@RegistroActivity,
                        "Cliente cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    // Tratamento de erro
                    response.errorBody()?.let { errorBody ->
                        try {
                            val errorJson = errorBody.string()
                            Log.e("ErrorResponse",errorJson) // Log completo do erro em formato JSON

                            val errorResponse = parseErrorResponse(errorJson)
                            if (errorResponse != null) {
                                val status = errorResponse.status
                                val msg = errorResponse.msg

                                // Captura o primeiro erro da lista de validações
                                val fieldError = errorResponse.listError.firstOrNull()
                                if (fieldError != null) {
                                    val fieldName = fieldError.fieldName
                                    val errorMessage = fieldError.message

                                    // Exibe o DIALOG com as informações do erro
                                    showErrorDialog("Erro no campo '$fieldName': $errorMessage\nStatus: $status")
                                    Log.e("retorno-erro-cadastro", msg)
                                } else {
                                    showErrorDialog("Erro: $msg\nStatus: $status")
                                }
                            } else {
                                showErrorDialog("Erro desconhecido ao cadastrar o cliente")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            showErrorDialog("Erro ao processar resposta.")
                        }
                        Log.e("Cadastro-error", "Erro ao tentar cadastrar o cliente.")
                        Toast.makeText(this@RegistroActivity,"Erro ao cadastrar cliente.",
                                        Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                progressBar.visibility = View.GONE
                // Erro na requisição
                Log.wtf("Cadastro-failure", "Falha na requisição de cadastro.")
                Toast.makeText(this@RegistroActivity, "Falha na comunicação.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    // Função para analisar a resposta de erro
    fun parseErrorResponse(errorJson: String): ErrorResponse? {
        return try {
            val gson = Gson()
            gson.fromJson(
                errorJson,
                ErrorResponse::class.java
            ) // Converte o JSON para ErrorResponse
        } catch (e: IOException) {
            e.printStackTrace()
            null // Retorna null se houver erro ao parsear
        }
    }

    fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Erro")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Fecha o dialog ao clicar no botão OK
            }
            .create()
            .show()
    }

    // Data class para a resposta de erro
    data class ErrorResponse(
        val status: Int,
        val msg: String,
        val listError: List<FieldError>
    )

    data class FieldError(
        val fieldName: String,
        val message: String
    )

}