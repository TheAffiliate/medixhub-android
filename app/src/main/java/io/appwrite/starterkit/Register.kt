package io.appwrite.starterkit

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {

    private lateinit var client: Client
    private lateinit var account: Account
    private lateinit var database: Databases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnRegister)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🧠 Appwrite setup
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("67768f3800342b6806fc")

        account = Account(client)
        database = Databases(client)

        // 🔌 UI Elements
        val fullName = findViewById<EditText>(R.id.editTextFullName)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val phone = findViewById<EditText>(R.id.editTextPhone)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val loginText = findViewById<TextView>(R.id.tvAlreadyHaveAccount)

        // 🔁 Go to Login
        loginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // 🧾 Register logic
        btnRegister.setOnClickListener {
            val name = fullName.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val userPhone = phone.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val selectedRadioId = radioGroup.checkedRadioButtonId
            val gender = if (selectedRadioId != -1) {
                findViewById<RadioButton>(selectedRadioId).text.toString()
            } else {
                "Other"
            }

            if (name.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Step 1: Create Account
                    val user = account.create(
                        userId = ID.unique(),
                        email = userEmail,
                        password = userPassword,
                        name = name
                    )

                    // Step 2: Login
                    account.createSession(
                        userId = userEmail,
                        secret = userPassword
                    )

                    // Step 3: Save patient record
                    database.createDocument(
                        databaseId = "677690be002cbee0009d",
                        collectionId = "67769103003333e7787f",
                        documentId = ID.unique(),
                        data = mapOf(
                            "userid" to user.id,
                            "email" to userEmail,
                            "phone" to userPhone,
                            "name" to name,
                            "gender" to gender,
                            "privacyConsent" to true
                        )
                    )

                    // Step 4: Go to book_appointment screen
                    runOnUiThread {
                        Toast.makeText(this@Register, "Registered & Logged In!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Register, book_apointment::class.java))
                        finish()
                    }

                } catch (e: AppwriteException) {
                    runOnUiThread {
                        Toast.makeText(this@Register, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
