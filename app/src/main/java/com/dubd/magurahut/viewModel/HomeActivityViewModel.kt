package com.dubd.magurahut.viewModel

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import co.lujun.androidtagview.TagView
import com.bumptech.glide.Glide
import com.dubd.magurahut.R
import com.dubd.magurahut.TinyDB
import com.dubd.magurahut.adapters.models.MainProduct
import com.dubd.magurahut.view.main.HomeActivity
import com.shashank.sony.fancytoastlib.FancyToast
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.product_item_dialog.view.*


class HomeActivityViewModel : ViewModel() {
    private val FACEBOOK_URL = "https://www.facebook.com/magurahut"
    private val FACEBOOK_PAGE_ID = "magurahut"

    fun getFacebookPageURL(context: Context): String? {
        val packageManager: PackageManager = context.packageManager
        return try {
            val versionCode =
                packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
            val activated =
                packageManager.getApplicationInfo("com.facebook.katana", 0).enabled
            if (activated) {
                if (versionCode >= 3002850) {
                    Log.d("main", "fb first url")
                    "fb://facewebmodal/f?href=$FACEBOOK_URL"
                } else {
                    "fb://page/$FACEBOOK_PAGE_ID"
                }
            } else {
                FACEBOOK_URL
            }
        } catch (e: PackageManager.NameNotFoundException) {
            FACEBOOK_URL
        }
    }

    fun watchYoutubeVideo(context: Context,id: String) {
        val appIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    fun openWhatsapp(context: Context, contact : String){
        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm: PackageManager = context.packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "আপনার ফোনে হোয়াটসঅ্যাপ নেই", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun showProductDialog(context: Context, product: MainProduct){
        val dialog = Dialog(context)
        val tb = TinyDB(context)
        val animation = AnimationUtils.loadAnimation(context,R.anim.fade)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.product_item_dialog,null)

        dialogView.mainText.text = product.name
        dialogView.rating.text = " "+product.rating
        dialogView.priceTextSell.text = "৳ "+product.price
        if(tb.all.containsKey(product.id.toString())){
            dialogView.productCount.text = tb.getListString(product.id.toString())[3]
        }

        dialogView.imgAdd.setOnClickListener {
            it.startAnimation(animation)
            var count = dialogView.productCount.text.toString().toInt()
            count++
            dialogView.productCount.text = count.toString()
        }

        dialogView.imgMinus.setOnClickListener {
            it.startAnimation(animation)
            var count = dialogView.productCount.text.toString().toInt()
            if(count!=1){
                count--
                dialogView.productCount.text = count.toString()
            }

        }
        when {
            product.inStock == "true" -> {
                dialogView.addToCart.setOnClickListener {
                    val stringList = arrayListOf<String>(
                        product.name,
                        product.price,
                        product.image,
                        dialogView.productCount.text.toString())

                    tb.putListString(product.id.toString(),stringList)

                    FancyToast.makeText(context,"কার্টে যোগ করা হয়েছে",FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show()
                }
            }
            product.price.trim()=="" -> {
                dialogView.addToCart.setOnClickListener {
                    FancyToast.makeText(
                        context,
                        "প্রোডাক্টি কার্টে অ্যাড করা যাচ্ছে না",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }
            else -> {
                dialogView.addToCart.text = "স্টক শেষ"
                dialogView.addToCart.setOnClickListener {
                    FancyToast.makeText(
                        context,
                        "এই প্রোডাক্টের স্টক নেই",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dialogView.descText.text = Html.fromHtml(product.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }else{
            dialogView.descText.text = Html.fromHtml(product.description)
        }

        dialogView.tagView.tags = product.categories.trim().split(",")
        dialogView.tagView.setOnTagClickListener(object : TagView.OnTagClickListener{
            override fun onTagClick(position: Int, text: String?) {

            }

            override fun onTagLongClick(position: Int, text: String?) {

            }

            override fun onSelectedTagDrag(position: Int, text: String?) {
            }

            override fun onTagCrossClick(position: Int) {

            }

        })

        Glide.with(context)
            .load(product.image)
            .centerCrop()
            .override(300,300)
            .placeholder(R.drawable.place_holder)
            .into(dialogView.img_main)

        dialog.setContentView(dialogView)
        dialog.show()
    }

    fun callPermissionCheck(context: Context, activity: HomeActivity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                val mDialog = MaterialDialog.Builder(activity)
                    .setTitle("কল পারমিশন প্রদান করুন")
                    .setMessage("কল করার জন্য প্রথমবারের মতো পারমিশন প্রদান করুন,")
                    .setCancelable(false)
                    .setPositiveButton(
                        "আচ্ছা", R.drawable.ic_delete
                    ) { diaInt, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            0
                        )
                        diaInt.dismiss()
                    }
                    .setNegativeButton(
                        "না। থাক।",
                        R.drawable.ic_clear
                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .build()

                mDialog.show()
            } else {
                ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.CALL_PHONE), 0)
            }
            false
        } else {
            true
        }
    }
}