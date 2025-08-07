package io.appwrite.starterkit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var client: Client
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Handle keyboard insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Appwrite init
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("67768f3800342b6806fc")

        account = Account(client)

        // 🧩 View bindings
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // ✅ Sign up redirect
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }

        // ✅ Forgot password dialog
        tvForgotPassword.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
            val editTextResetEmail = dialogView.findViewById<EditText>(R.id.editTextResetEmail)

            AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val email = editTextResetEmail.text.toString().trim()
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                account.createRecovery(
                                    email = email,
                                    url = "https://your-frontend-url.com/reset-password"
                                )
                                runOnUiThread {
                                    Toast.makeText(this@Login, "Password reset email sent.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: AppwriteException) {
                                runOnUiThread {
                                    Toast.makeText(this@Login, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ✅ Login button logic
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    account.createEmailPasswordSession(
                        email = email,
                        password = password
                    )

                    // ✅ Save session locally
                    val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putBoolean("is_logged_in", true)
                        apply()
                    }

                    runOnUiThread {
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login, book_apointment::class.java))
                        finish()
                    }

                } catch (e: AppwriteException) {
                    runOnUiThread {
                        Toast.makeText(this@Login, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
