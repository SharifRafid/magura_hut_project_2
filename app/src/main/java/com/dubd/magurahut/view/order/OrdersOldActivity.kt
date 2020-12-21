package com.dubd.magurahut.view.order

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.dubd.magurahut.R
import com.dubd.magurahut.adapters.models.CartItem
import com.dubd.magurahut.view.order.ui.main.SectionsPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrdersOldActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var arrayList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_old)
        FirebaseDatabase.getInstance()
            .getReference("users/"+firebaseAuth.currentUser?.phoneNumber.toString())
            .child("orders").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    arrayList = ArrayList()
                    for(snap in snapshot.children){
                        arrayList.add(snap.key.toString())
                    }
                    Log.e("ORDERS ARRAY",arrayList.toString())
                    val sectionsPagerAdapter = SectionsPagerAdapter(this@OrdersOldActivity, supportFragmentManager)
                    val viewPager: ViewPager = findViewById(R.id.view_pager)
                    viewPager.adapter = sectionsPagerAdapter
                    val tabs: TabLayout = findViewById(R.id.tabs)
                    tabs.setupWithViewPager(viewPager)
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }

            })
    }

    fun getKeyArray() : ArrayList<String>{
        return arrayList
    }
}