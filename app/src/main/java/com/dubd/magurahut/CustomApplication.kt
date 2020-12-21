package com.dubd.magurahut

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.multidex.MultiDex
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.instacart.library.truetime.TrueTime
import io.customerly.Customerly

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Customerly.configure(
            application = this, customerlyAppId = "039e4f8c", widgetColorInt = Color.parseColor(
                "#689F38"
            )
        )
        Thread {
            while (true) {
                try{
                    TrueTime.build().initialize()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
        }.start()
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}