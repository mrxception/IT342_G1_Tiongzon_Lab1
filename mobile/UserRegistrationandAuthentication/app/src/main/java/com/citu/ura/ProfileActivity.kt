package com.citu.ura

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ProfileActivity : Activity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnLogout: Button

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivAvatar   = findViewById(R.id.ivAvatar)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail    = findViewById(R.id.tvEmail)
        tvRole     = findViewById(R.id.tvRole)
        tvStatus   = findViewById(R.id.tvStatus)
        btnLogout  = findViewById(R.id.btnLogout)

        loadUserProfile()

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token.isNullOrBlank()) {
            Toast.makeText(this, "No token found. Please log in again.", Toast.LENGTH_LONG).show()
            goToLogin()
            return
        }

        Thread {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("http://10.0.2.2:8080/api/users/me")
                conn = url.openConnection() as HttpURLConnection

                conn.apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
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
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                updateProfileUI(jsonResponse)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "Invalid profile data format",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        responseCode == 401 || responseCode == 403 -> {
                            val errorMsg = try {
                                val jsonError = JSONObject(responseBody)
                                jsonError.optString("message", "Session expired")
                            } catch (e: Exception) {
                                "Unauthorized - please log in again"
                            }
                            Toast.makeText(this@ProfileActivity, errorMsg, Toast.LENGTH_LONG).show()
                            logout()
                        }

                        else -> {
                            Toast.makeText(
                                this@ProfileActivity,
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
                    Toast.makeText(this@ProfileActivity, msg, Toast.LENGTH_LONG).show()
                }
            } finally {
                conn?.disconnect()
            }
        }.start()
    }

    private fun updateProfileUI(json: JSONObject) {
        tvUsername.text = json.optString("username", "—")
        tvEmail.text    = json.optString("email",    "—")
        tvRole.text     = json.optString("role",     "—")
        tvStatus.text   = json.optString("status",   "Active")

        val avatarUrl = json.optString("avatar", "")
        if (avatarUrl.isNotBlank()) {
            // ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            // tvStatus.text = "Avatar URL: $avatarUrl"
        }
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        sharedPref.edit().remove("token").apply()

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}