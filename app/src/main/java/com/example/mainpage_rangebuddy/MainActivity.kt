package com.example.mainpage_rangebuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.view.Window
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    //variables called
    private lateinit var ToInformationPage: ImageButton
    private lateinit var AutomaticPictureMode: Button
    private lateinit var ManualPictureMode: Button
    private lateinit var PastSessionsPage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        //button to information page
        ToInformationPage = findViewById(R.id.Information)
        ToInformationPage.setOnClickListener {
            val ToHelp = Intent(this, HelpPage::class.java)
            startActivity(ToHelp)
        }
        //button to PicturePage
        AutomaticPictureMode = findViewById(R.id.AutomaticToPicturePage)
        AutomaticPictureMode.setOnClickListener {
            val Auto = Intent(this, PhotoPage::class.java)
            startActivity(Auto)
        }
        //button to PicturePage
        ManualPictureMode = findViewById(R.id.ManualToPicturePage)
        ManualPictureMode.setOnClickListener {
            val Manual = Intent(this, PhotoPage::class.java)
            startActivity(Manual)
        }
        //button to PastSessionsPage
        PastSessionsPage = findViewById(R.id.ToPastSessionsPage)
        PastSessionsPage.setOnClickListener {
            val Past = Intent(this, PastSessions::class.java)
            startActivity(Past)


        }



    }
}