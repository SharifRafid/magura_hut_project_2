package com.dubd.magurahut.view.main

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.dubd.magurahut.R
import com.dubd.magurahut.view.extras.AboutActivity
import com.dubd.magurahut.view.fragments.AllProducts
import com.dubd.magurahut.viewModel.HomeActivityViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import io.customerly.Customerly
import kotlinx.android.synthetic.main.activity_home.*
import java.io.IOException


class HomeActivity : AppCompatActivity() {

    private lateinit var homeActivityViewModel : HomeActivityViewModel
    private var fabOpen = false
    private val translationY = 100f
    private lateinit var animation : Animation
    private val interpolator = OvershootInterpolator()
    private lateinit var fragment : AllProducts
    private var category = ""
    private var internetAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setTheme(R.style.MyTheme)
        fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerMain) as AllProducts
        initCustomFab()
        initSearchView()
        homeActivityViewModel = HomeActivityViewModel()
        basicInit()
        internetAvailable = checkInternet()
    }

    private fun checkInternet(): Boolean {
        return try {
            if (internetCheck()) {
                true
            } else {
                showDia()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showDia()
            false
        }
    }

    private fun showDia() {
        AlertDialog.Builder(this).setTitle("ইন্টারনেট নেই")
            .setMessage("অ্যাপটি চালাতে ইন্টারনেট প্রয়োজন").setCancelable(false)
            .setPositiveButton("কল করুন") { _: DialogInterface?, _: Int ->
                if (homeActivityViewModel.callPermissionCheck(this, this)) {
                    val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+8801777237575"))
                    startActivity(callIntent)
                }
            }
            .setNegativeButton("ঠিক আছে"){ _: DialogInterface?, _: Int -> finish() }
            .create().show()
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun internetCheck(): Boolean {
        val cmd = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(cmd).waitFor() == 0
    }

    private fun basicInit() {
        val sharedPreferences = getSharedPreferences("launch", MODE_PRIVATE)

        if(!sharedPreferences.all.containsKey("firstLaunch")){
            TapTargetSequence(this)
                .targets(
                    TapTarget.forView(
                        categories,
                        "ক্যাটাগরি গুলো",
                        "আমাদের ক্যাটাগরি গুলো দেখতে এখানে ক্লিক করুন"
                    ),
                    TapTarget.forView(
                        search_start_button,
                        "সার্চ করুন",
                        "এখানে ক্লিক করে সার্চ করুন"
                    ),
                    TapTarget.forView(fabMain, "মেসেজ করুন", "এখানে ক্লিক করে মেসেজ আমাদের করুন"),
                    TapTarget.forView(callNow, "কল করুন", "এখানে ক্লিক করে আমাদের কল করুন"),
                    TapTarget.forView(
                        linFab,
                        "আপনার কার্টের প্রোডাক্ট গুলো দেখুন",
                        "এখানে ক্লিক করে কার্টের প্রোডাক্ট গুলো দেখুন"
                    ),
                    TapTarget.forView(mr_more_main, "আরও অপশন", "এখানে ক্লিক করে আরও অপশন দেখুন")
                ).continueOnCancel(true)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        sharedPreferences.edit().putString("firstLaunch", "false").apply()
                        ActivityCompat.requestPermissions(
                            this@HomeActivity,
                            arrayOf(
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ),
                            0
                        )
                    }

                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {

                    }

                    override fun onSequenceCanceled(lastTarget: TapTarget?) {

                    }
                })
                .start()
        }

        categories.setOnClickListener {
            it.startAnimation(animation)
            if(!drawer.isDrawerOpen(GravityCompat.START)){
                drawer.openDrawer(GravityCompat.START)
            }else{
                drawer.closeDrawer(GravityCompat.START)
            }
        }

        linFab.setOnClickListener{
            it.startAnimation(animation)
            val toCart = Intent(this@HomeActivity, CartActivity::class.java)
            startActivity(toCart)
        }

        callNow.setOnClickListener {
            it.startAnimation(animation)
            if (homeActivityViewModel.callPermissionCheck(this, this)) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+8801777237575"))
                startActivity(callIntent)
            }
        }

        if(fragment.isAdded){
            fragment.filter(search_view.text.toString(), category)
        }
    }

    fun catVisibleClick(view: View){
        val linear = findViewById<LinearLayout>(
            resources.getIdentifier(
                view.resources.getResourceName(
                    view.id
                ).toString() + "Linear", "id", packageName
            )
        )
        if(linear.visibility==View.VISIBLE){
            linear.visibility = View.GONE
        }else{
            linear.visibility = View.VISIBLE
        }
    }

    fun catClicked(view: View) {
        category = view.contentDescription.toString().trim()
        if(fragment.isAdded){
            fragment.filter(search_view.text.toString(), category)
        }
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    private fun initSearchView() {

        search_start_button.setOnClickListener {
            titleText.visibility = View.GONE
            search_view.visibility = View.VISIBLE
            search_start_button.visibility = View.GONE
            search_close_button.visibility = View.VISIBLE
        }

        search_close_button.setOnClickListener {
            titleText.visibility = View.VISIBLE
            search_view.visibility = View.GONE
            search_start_button.visibility = View.VISIBLE
            search_close_button.visibility = View.GONE
            search_view.text.clear()
            if(fragment.isAdded){
                fragment.filter(search_view.text.toString(), category)
            }
        }

        search_view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (fragment.isAdded) {
                    fragment.filter(search_view.text.toString(), category)
                }
            }
        })

        search_view.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if(fragment.isAdded){
                    fragment.filter(search_view.text.toString(), category)
                }
                true
            } else false
        }
    }

    private fun initCustomFab() {
        animation = AnimationUtils.loadAnimation(this@HomeActivity, R.anim.fade)

        mr_facebook.visibility = View.GONE
        mr_wapp.visibility = View.GONE
        mr_mail.visibility = View.GONE
        mr_more.visibility = View.GONE

        mr_facebook.alpha = 0f
        mr_wapp.alpha = 0f
        mr_mail.alpha = 0f
        mr_more.alpha = 0f

        mr_facebook.translationY = translationY
        mr_wapp.translationY = translationY
        mr_mail.translationY = translationY
        mr_more.translationY = translationY

        mr_facebook.setOnClickListener {
            mr_facebook.startAnimation(animation)

            val facebookIntent = Intent(Intent.ACTION_VIEW)
            val facebookUrl = homeActivityViewModel.getFacebookPageURL(this@HomeActivity)
            facebookIntent.data = Uri.parse(facebookUrl)
            startActivity(facebookIntent)
        }

        mr_wapp.setOnClickListener {
            mr_wapp.startAnimation(animation)
            homeActivityViewModel.openWhatsapp(this, "+8801777237575")
        }

        mr_mail.setOnClickListener {
            mr_mail.startAnimation(animation)

            homeActivityViewModel.watchYoutubeVideo(this, "a-TfKVDEpOU");
        }

        mr_more.setOnClickListener {
            mr_more.startAnimation(animation)

            startActivity(Intent(this, AboutActivity::class.java))
        }

        fabMain.setOnClickListener{
            Customerly.openSupport(activity = this@HomeActivity)
        }

        mr_more_main.setOnClickListener {
            if(fabOpen){
                fabOpen = !fabOpen

                mr_more_main.startAnimation(animation)

                //close the menu
                mr_facebook.animate().translationY(translationY).alpha(0f).setInterpolator(
                    interpolator
                ).setDuration(300).start()
                mr_wapp.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                    300
                ).start()
                mr_mail.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                    300
                ).start()
                mr_more.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                    300
                ).start()

                mr_facebook.visibility = View.GONE
                mr_wapp.visibility = View.GONE
                mr_mail.visibility = View.GONE
                mr_more.visibility = View.GONE
            }else{
                fabOpen = !fabOpen //change the boolean value

                //enable them

                mr_facebook.visibility = View.VISIBLE
                mr_wapp.visibility = View.VISIBLE
                mr_mail.visibility = View.VISIBLE
                mr_more.visibility = View.VISIBLE

                mr_more_main.startAnimation(animation)

                //open the menu
                mr_facebook.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(
                    300
                ).start()
                mr_wapp.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(
                    300
                ).start()
                mr_mail.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(
                    300
                ).start()
                mr_more.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(
                    300
                ).start()

            }
        }
    }

    override fun onBackPressed() {
        if(fabOpen){
            fabOpen = !fabOpen

            mr_more_main.startAnimation(animation)

            //close the menu
            mr_facebook.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                300
            ).start()
            mr_wapp.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                300
            ).start()
            mr_mail.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                300
            ).start()
            mr_more.animate().translationY(translationY).alpha(0f).setInterpolator(interpolator).setDuration(
                300
            ).start()

            mr_facebook.visibility = View.GONE
            mr_wapp.visibility = View.GONE
            mr_mail.visibility = View.GONE
            mr_more.visibility = View.GONE
        }else
        {
            if(search_close_button.visibility==View.VISIBLE){
                titleText.visibility = View.VISIBLE
                search_view.visibility = View.GONE
                search_start_button.visibility = View.VISIBLE
                search_close_button.visibility = View.GONE
                search_view.text.clear()
                if(fragment.isAdded){
                    fragment.filter(search_view.text.toString(), category)
                }
            }else{
                if(category!=""){
                    category =  ""
                    if(fragment.isAdded){
                        fragment.filter(search_view.text.toString(), category)
                    }
                }else{
                    if(drawer.isDrawerOpen(GravityCompat.START)){
                        drawer.closeDrawer(GravityCompat.START)
                    }else{
                        finish()
                    }
                }
            }
        }
    }
}