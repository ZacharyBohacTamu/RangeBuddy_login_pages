package com.example.mainpage_rangebuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Window
import android.widget.Button
import android.widget.Toast

class SessionsData : AppCompatActivity() {

    private lateinit var toMainPage: Button
    private lateinit var pastSessionsPage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        //set content view
        setContentView(R.layout.activity_sessions_data)

        //save session
        pastSessionsPage = findViewById(R.id.save_Session)
        pastSessionsPage.setOnClickListener {
            Toast.makeText(this, "Choose which session to save on", Toast.LENGTH_SHORT).show()
            val session = Intent(this, PastSessions::class.java)
            startActivity(session)
        }
        //back to main page
        toMainPage = findViewById(R.id.BacktoMainPage)
        toMainPage.setOnClickListener {
            val home = Intent(this, MainActivity::class.java)
            startActivity(home)
        }


    }
}