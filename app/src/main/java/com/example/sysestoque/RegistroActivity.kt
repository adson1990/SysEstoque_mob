package com.example.sysestoque

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog


class RegistroActivity : AppCompatActivity() {

    private lateinit var edtCPF: EditText
    private lateinit var edtSenha: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var linearLayoutPhoneNumbers: LinearLayout
    private lateinit var imageButton: ImageButton
    private lateinit var spinnerCelphone: Spinner
    private var phoneNumberCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nome com letras maiúsculas
        val edtNome = findViewById<EditText>(R.id.edtNomeCompleto)
        edtNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtNome.removeTextChangedListener(this) // Previne loops infinitos
                edtNome.setText(s.toString().uppercase())
                edtNome.setSelection(s?.length ?: 0) // Posiciona o cursor no final
                edtNome.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // validação do e-mail digitado
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtEmail.removeTextChangedListener(this) // remove o listener temporariamente para evitar loop

                // Verifica se o e-mail é válido conforme é digitado
                val emailInput = s.toString()

                // regex para verificar a terminação do e-mail após o "@"
                val emailPattern = Regex(".+@.+\\.com.*")

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput)
                        .matches() && emailPattern.containsMatchIn(emailInput)
                ) {
                    edtEmail.setTextColor(
                        ContextCompat.getColor(
                            this@RegistroActivity,
                            R.color.green_600
                        )
                    )
                } else {
                    // edtEmail.setTextColor(resources.getColor(R.color.red_700, theme)) Mais moderno porém requer API(M)
                    edtEmail.setTextColor(
                        ContextCompat.getColor(
                            this@RegistroActivity,
                            R.color.red_700
                        )
                    )
                }

                edtEmail.setSelection(emailInput.length) // Coloca o cursor no final do texto
                edtEmail.addTextChangedListener(this) // Reatribui o listener
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Máscara e validação do CPF
        edtCPF = findViewById<EditText>(R.id.edtCPF)
        edtCPF.addCpfMask()

        //Validação da composição da senha
        edtSenha = findViewById(R.id.edtSenha)
        edtSenha.validPassword()

        val rbMasculino = findViewById<RadioButton>(R.id.rbMasculino)
        val rbFeminino = findViewById<RadioButton>(R.id.rbFeminino)

        val edtSalario = findViewById<EditText>(R.id.edtSalario)
        edtSalario.setText(R.string.sifrao)

        // Salário obrigatoriamente começa com R$
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

        val edtNascimento = findViewById<EditText>(R.id.edtNascimento)

        //opção de países do endereço
        spinnerCountry = findViewById(R.id.spinnerCountry)

        val countriesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        )

        countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = countriesAdapter

        //Adicionando mais números de telefone
        linearLayoutPhoneNumbers = findViewById(R.id.linearLayoutPhoneNumbers)
        imageButton = findViewById(R.id.imageButton)

        spinnerCelphone = findViewById(R.id.spinnerTipoNumero)

        val celphoneAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_telefone,
            android.R.layout.simple_spinner_item
        )
        celphoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCelphone.adapter = celphoneAdapter

        imageButton.setOnClickListener {
            if (phoneNumberCount < 3) { // Permite até 3 números de contato
                addPhoneNumberFields()
                phoneNumberCount++
            } else {
                imageButton.isEnabled = false // Desativa o botão quando atingir o limite
            }
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
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }
    }

    fun EditText.addCpfMask() {
        this.addTextChangedListener(object : TextWatcher {
            private var isUpdating: Boolean =
                false //flag para verificar se o TextWatcher foi acionado
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
                this@addCpfMask.setSelection(formattedText.length) // garantir que o cursor estara no final da string
            }

            override fun afterTextChanged(s: Editable?) {
                edtCPF.removeTextChangedListener(this)
                val cpf = s.toString().replace(".", "").replace("-", "")
                if (cpf.length == 11) {
                    if (isCPF(cpf)) {
                        Toast.makeText(this@RegistroActivity, "CPF válido", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@RegistroActivity, "CPF inválido", Toast.LENGTH_LONG)
                            .show()
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
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
            hint = "DDD"
        }

        // Cria o EditText para o número de telefone
        val phoneNumberEditText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.6f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
            hint = getString(R.string.number)
        }

        // Cria o Spinner para o tipo de número
        val tipoPhoneNumberSpinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.size_5dp)
                marginEnd = resources.getDimensionPixelSize(R.dimen.size_5dp)
            }
        }

        newLinearLayout.addView(dddEditText)
        newLinearLayout.addView(phoneNumberEditText)
        newLinearLayout.addView(tipoPhoneNumberSpinner)

        linearLayoutPhoneNumbers.addView(newLinearLayout)
        Log.i("AddNewPhone","Novo número adicionado")
    }
}