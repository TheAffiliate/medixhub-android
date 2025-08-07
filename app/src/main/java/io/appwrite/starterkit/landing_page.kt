package io.appwrite.starterkit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class landing_page : AppCompatActivity() {

    private lateinit var client: Client
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Appwrite initialization
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("67768f3800342b6806fc") // 🔁 Replace with your Project ID

        account = Account(client)

        // ✅ Coroutine for session check
        CoroutineScope(Dispatchers.IO).launch {
            try {
                account.get() // Validates existing session

                // 🎯 Session is valid — Go to booking page
                runOnUiThread {
                    startActivity(Intent(this@landing_page, book_apointment::class.java))
                    finish()
                }

            } catch (e: AppwriteException) {
                // 😢 Session invalid or expired — show landing page
                runOnUiThread {
                    // Load the landing page layout
                    setContentView(R.layout.activity_landing_page)

                    // Now safely access views
                    val getStartedButton = findViewById<Button>(R.id.getStartedButton)
                    val skipButton = findViewById<Button>(R.id.skipButton)

                    // Navigate to Register
                    getStartedButton.setOnClickListener {
                        startActivity(Intent(this@landing_page, Register::class.java))
                    }

                    // Navigate to Login
                    skipButton.setOnClickListener {
                        startActivity(Intent(this@landing_page, Login::class.java))
                    }
                }
            }
        }
    }
}
