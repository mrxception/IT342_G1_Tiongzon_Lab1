package com.citu.ura

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val regBtn = findViewById<Button>(R.id.regBtn)
        regBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val etUsername = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val btnLogin   = findViewById<Button>(R.id.loginBtn)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }.toString()

                val url = URL("http://10.0.2.2:8080/api/auth/login")
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
                             val jsonResponse = JSONObject(responseBody)
                             val token = jsonResponse.getString("token")

                             val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                             val editor = sharedPref.edit()
                             editor.putString("token", token)
                             editor.apply()

                            Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                             val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                             startActivity(intent)
                             finish()
                        }

                        responseCode == 400 || responseCode == 401 -> {
                            val errorMessage = try {
                                val jsonError = JSONObject(responseBody)
                                jsonError.optString("message", "Authentication failed")
                            } catch (e: Exception) {
                                responseBody.ifBlank { "Authentication failed" }
                            }

                            Toast.makeText(
                                this@MainActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        else -> {
                            Toast.makeText(
                                this@MainActivity,
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
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}