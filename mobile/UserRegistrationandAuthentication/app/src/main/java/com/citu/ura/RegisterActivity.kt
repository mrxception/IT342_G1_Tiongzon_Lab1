package com.citu.ura

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val etUsername = findViewById<EditText>(R.id.username)
        val etEmail = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.password)
        val regBtn   = findViewById<Button>(R.id.regBtn)

        regBtn.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() ||  password.isEmpty()) {
                Toast.makeText(this, "Please enter username, email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, email, password)
        }
    }

    private fun performLogin(username: String, email: String, password: String) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("username", username)
                    put("email", email)
                    put("password", password)
                }.toString()

                val url = URL("http://10.0.2.2:8080/api/auth/register")
                val conn = url.openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                conn.outputStream.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = conn.responseCode
                val responseBody = try {
                    if (responseCode in 200..299) {
                        conn.inputStream.use { it.bufferedReader().readText() }
                    } else {
                        conn.errorStream?.use { it.bufferedReader().readText() } ?: ""
                    }
                } catch (e: Exception) {
                    "Could not read response"
                }

                handler.post {
                    when {
                        responseCode in 200..299 -> {


                            Toast.makeText(
                                this@RegisterActivity,
                                "Register successful!",
                                Toast.LENGTH_SHORT
                            ).show()
//                            val intent = Intent(this@RegisterActivity, ProfileActivity::class.java)
//                            startActivity(intent)
//                            finish()
                        }

                        responseCode == 400 || responseCode == 401 -> {
                            val errorMessage = try {
                                val jsonError = JSONObject(responseBody)
                                jsonError.optString("message", "Authentication failed")
                            } catch (e: Exception) {
                                responseBody.ifBlank { "Authentication failed" }
                            }

                            Toast.makeText(
                                this@RegisterActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Server error ($responseCode): $responseBody",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    val msg = when (e) {
                        is java.net.UnknownHostException -> "Cannot reach server (check IP/port)"
                        is java.net.ConnectException -> "Connection refused - is backend running?"
                        else -> "Connection error: ${e.message ?: e.javaClass.simpleName}"
                    }
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}