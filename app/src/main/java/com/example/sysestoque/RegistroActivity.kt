package com.example.sysestoque

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class RegistroActivity : AppCompatActivity() {

    private lateinit var edtCPF: EditText

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

            override fun onTextChanged (s: CharSequence?, start: Int, before: Int, count: Int){
                edtEmail.removeTextChangedListener(this) // remove o listener temporariamente para evitar loop

                // Verifica se o e-mail é válido conforme é digitado
                val emailInput = s.toString()

                // regex para verificar a terminação do e-mail após o "@"
                val emailPattern = Regex(".+@.+\\.com.*")

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() && emailPattern.containsMatchIn(emailInput)) {
                    edtEmail.setTextColor(ContextCompat.getColor(this@RegistroActivity, R.color.green_600)) // Exemplo: muda a cor do texto para verde se válido
                } else {
                    edtEmail.setTextColor(ContextCompat.getColor(this@RegistroActivity, R.color.red_700)) // Exemplo: muda a cor do texto para vermelho se inválido
                }

                edtEmail.setSelection(emailInput.length) // Coloca o cursor no final do texto
                edtEmail.addTextChangedListener(this) // Reatribui o listener
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        edtCPF = findViewById<EditText>(R.id.edtCPF)
        edtCPF.addCpfMask()

    // fim oncreate
    }

    fun EditText.addCpfMask() {
        this.addTextChangedListener(object : TextWatcher {
            private var isUpdating: Boolean = false
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

    fun isCPF(CPF: String): Boolean {
        if (CPF == "00000000000" ||
            CPF == "11111111111" ||
            CPF == "22222222222" ||
            CPF == "33333333333" ||
            CPF == "44444444444" ||
            CPF == "55555555555" ||
            CPF == "66666666666" ||
            CPF == "77777777777" ||
            CPF == "88888888888" ||
            CPF == "99999999999" ||
            (CPF.length != 11)
        ) return (false)

        try {
            val numbers = CPF.map { it.toString().toInt() }

            val firstNineDigits = numbers.subList(0, 9)
            val firstCheckerDigit = calculateDigit(firstNineDigits, 10)

            val firstTenDigits = numbers.subList(0, 10)
            val secondCheckerDigit = calculateDigit(firstTenDigits, 11)

            return firstCheckerDigit == numbers[9] && secondCheckerDigit == numbers[10]
        } catch (e: Exception) {
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
}