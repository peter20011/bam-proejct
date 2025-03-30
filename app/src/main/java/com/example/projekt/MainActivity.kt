package com.example.projekt

import android.annotation.SuppressLint
import android.os.Bundle

import android.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        SecurityChecker.isUnsafe()?.let { reason ->
            AlertDialog.Builder(this)
                .setTitle("Uwaga!")
                .setMessage("Twoje urządzenie nie jest bezpieczne, bo $reason.")
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .show()
        }

        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        loadButton = findViewById(R.id.loadButton)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        clearButton = findViewById(R.id.clearButton)


        saveButton.setOnClickListener {
            val text = editText.text.toString()
            val cipher = CryptoManager.getEncryptCipher()

            BiometricHelper.authenticate(
                this,
                "Potwierdź tożsamość, aby zapisać dane",
                cipher,
                onSuccess = { authedCipher ->
                    val encrypted = CryptoManager.encryptWithCipher(text, authedCipher)
                    FileManager.saveEncrypted(this, encrypted)
                    editText.setText("")
                    Toast.makeText(this, "Zapisano", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(this, "Anulowano", Toast.LENGTH_SHORT).show()
                }
            )
        }

        loadButton.setOnClickListener {
            val data = FileManager.loadEncrypted(this)
            if (data == null) {
                Toast.makeText(this, "Brak danych", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val iv = data.copyOfRange(0, 12)
            val cipher = CryptoManager.getDecryptCipher(iv)

            BiometricHelper.authenticate(
                this,
                "Potwierdź tożsamość, aby odczytać dane",
                cipher,
                onSuccess = { authedCipher ->
                    val decrypted = CryptoManager.decryptWithCipher(data, authedCipher)
                    editText.setText(decrypted)
                },
                onError = {
                    Toast.makeText(this, "Anulowano", Toast.LENGTH_SHORT).show()
                }
            )
        }

        exportButton.setOnClickListener {
            val cipher = CryptoManager.getEncryptCipher()

            BiometricHelper.authenticate(
                this,
                "Uwierzytelnij się, aby wyeksportować dane",
                cipher,
                onSuccess = {
                    askPassword("Podaj hasło do eksportu") { password ->
                        val text = editText.text.toString()
                        val exported = CryptoManager.exportEncrypted(text, password)
                        FileManager.exportData(this).writeBytes(exported)
                        editText.setText("")
                        Toast.makeText(this, "Wyeksportowano", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    Toast.makeText(this, "Anulowano eksport", Toast.LENGTH_SHORT).show()
                }
            )
        }

        importButton.setOnClickListener {
            val cipher = CryptoManager.getEncryptCipher()

            BiometricHelper.authenticate(
                this,
                "Uwierzytelnij się, aby zaimportować dane",
                cipher,
                onSuccess = {
                    askPassword("Podaj hasło do importu") { password ->
                        try {
                            val file = FileManager.exportData(this)
                            val imported = file.readBytes()
                            val text = CryptoManager.importEncrypted(imported, password)
                            editText.setText(text)
                            Toast.makeText(this, "Zaimportowano", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Błąd importu", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onError = {
                    Toast.makeText(this, "Anulowano import", Toast.LENGTH_SHORT).show()
                }
            )
        }

        clearButton.setOnClickListener {
            val cipher = CryptoManager.getEncryptCipher()

            BiometricHelper.authenticate(
                this,
                "Uwierzytelnij się, aby wyczyścić dane",
                cipher,
                onSuccess = {
                    FileManager.clearData(this)
                    CryptoManager.deleteKey()
                    editText.setText("")
                    Toast.makeText(this, "Wyczyszczono", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(this, "Anulowano czyszczenie", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

        @SuppressLint("SuspiciousIndentation")
        private fun askPassword(title: String, callback: (String) -> Unit) {
        val input = EditText(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ -> callback(input.text.toString()) }
            .setNegativeButton("Anuluj", null)
            .create()

            dialog.window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )

            dialog.show()
    }

    override fun onPause() {
        super.onPause()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun onStop() {
        super.onStop()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun onResume() {
        super.onResume()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}
