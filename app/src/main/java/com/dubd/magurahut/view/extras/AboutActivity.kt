package com.dubd.magurahut.view.extras

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dubd.magurahut.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setTheme(R.style.MyTheme)
    }
}