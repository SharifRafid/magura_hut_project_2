package com.dubd.magurahut.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.dubd.magurahut.R
import com.dubd.magurahut.TinyDB
import com.dubd.magurahut.adapters.models.CartItem
import com.dubd.magurahut.view.main.CartActivity
import com.shreyaspatil.MaterialDialog.MaterialDialog

class CartViewModel : ViewModel() {

    fun callPermissionCheck(context: Context, activity: CartActivity): Boolean {
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


    fun getCartItems(context: Context) : ArrayList<CartItem>{
        val tb = TinyDB(context)
        val cartItems : ArrayList<CartItem> = ArrayList()
        for (item in tb.all.keys){
            val arrayList = tb.getListString(item)
            cartItems.add(
                CartItem(
                    item,
                    arrayList[0],
                    arrayList[2],
                    arrayList[1].toInt(),
                    arrayList[3].toInt()
                )
            )
        }
        return cartItems
    }

    fun getAllCost(context: Context) : Int{
        val tb = TinyDB(context)
        var i = 0
        for (item in tb.all.keys){
            i += (tb.getListString(item)[1].toInt()*tb.getListString(item)[3].toInt())
        }
        return i
    }

    fun getAllCount(context: Context) : Int{
        val tb = TinyDB(context)
        return tb.all.keys.size
    }


    fun deleteCartItem(context: Context, id : String){
        val tinyDB = TinyDB(context)
        if(tinyDB.all.keys.contains(id)){
            tinyDB.remove(id)
        }
    }

    fun deleteAll(context: Context){
        val tinyDB = TinyDB(context)
        tinyDB.clear()
    }
}