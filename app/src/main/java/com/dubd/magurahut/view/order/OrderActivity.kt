package com.dubd.magurahut.view.order

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.dubd.magurahut.R
import com.dubd.magurahut.view.main.HomeActivity
import com.dubd.magurahut.viewModel.CartViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.instacart.library.truetime.TrueTime
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.pin_dia.view.*
import mumayank.com.airlocationlibrary.AirLocation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class OrderActivity : AppCompatActivity() {
    private val cartViewModel = CartViewModel()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var animation: Animation
    private var locationStatus = 0
    private var lat = ""
    private var lang = ""
    private var pinString = ""
    private var userName = ""
    private var time = "Time Not Found"

    private val airLocation = AirLocation(this, object : AirLocation.Callback {

        override fun onSuccess(locations: ArrayList<Location>) {
            locationStatus = 1
            lat = locations[0].latitude.toString()
            lang = locations[0].longitude.toString()
            Log.e("LOCATION", "$lat,$lang")
            location.text = "আপনার লোকেশন ডিটেক্ট করা গিয়েছে। ধন্যবাদ"
        }

        override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
            locationStatus = 2
            location.text = "আপনার লোকেশন ডিটেক্ট করা যায়নি আবার চেষ্টা করতে ক্লিক করুন"
        }

    },true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        setTheme(R.style.MyTheme)

        val dtm: Date = TrueTime.now()
        val localDateFormat = SimpleDateFormat("dd-MM-yyyy::HH:mm")
        time = localDateFormat.format(dtm) as String

        airLocation.start()

        location.setOnClickListener {
            it.startAnimation(animation)
            location.text = "দ্রুততম ডেলিভারির জন্য আপনার লোকেশন ডিটেক্ট করা হচ্ছে......"
            airLocation.start()
        }

        phoneCodeLinear.visibility = View.GONE
        userInfoLinear.visibility = View.GONE
        orderNow.visibility = View.GONE

        animation = AnimationUtils.loadAnimation(this, R.anim.fade)
        val finalBill = createFinalOrderBill()
        orderFinal.text = finalBill

        if(firebaseAuth.currentUser!=null){
            askName(firebaseAuth.currentUser!!)
        }else{
            phoneCodeLinear.visibility = View.VISIBLE
            userInfoLinear.visibility = View.GONE
            orderNow.visibility = View.GONE
        }

        verifyCode.setOnClickListener {
            if(editTextCode.text.toString().trim().isNotEmpty()){
                val whitelistedCountries: List<String> = listOf("+880")
                val phoneConfig = AuthUI.IdpConfig.PhoneBuilder()
                    .setWhitelistedCountries(whitelistedCountries)
                    .setDefaultNumber("+88" + editTextCode.text.toString().trim())
                    .build()
                val providers = arrayListOf(phoneConfig)
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    0
                )
            }
        }

        signUp.setOnClickListener {
            if(name.text?.isNotEmpty()!!){
                if(pin.text?.isNotEmpty()!!){
                    signUp.isEnabled = false
                    databaseReference.child(firebaseAuth.currentUser?.phoneNumber.toString())
                        .child("userId").setValue(firebaseAuth.currentUser?.uid)
                    databaseReference.child(firebaseAuth.currentUser?.phoneNumber.toString())
                        .child("pin").setValue(pin.text.toString())
                    databaseReference.child(firebaseAuth.currentUser?.phoneNumber.toString())
                        .child("userName").setValue(name.text.toString()).addOnCompleteListener {
                            phoneCodeLinear.visibility = View.GONE
                            userInfoLinear.visibility = View.GONE
                            orderNow.visibility = View.VISIBLE
                            user_name.text = "স্বাগতম "+name.text.toString()
                            pinString = pin.text.toString()
                            userName = name.text.toString()
                            user_name.text = "স্বাগতম "+name.text.toString()+
                                    "\n আপনার ফোন নাম্বারঃ "+ firebaseAuth.currentUser?.phoneNumber.toString()
                            change_number.setOnClickListener{
                                it.startAnimation(animation)
                                firebaseAuth.signOut()
                                finish()
                            }
                        }
                }else{
                    pin.error = "পিন দিন"
                }
            }else{
                name.error = "নাম লিখুন"
            }
        }

        orderNowButton.setOnClickListener {
            if(address.text?.isNotEmpty()!!){
                if(pinString.isNotEmpty()){
                    val dialog = Dialog(this)
                    val view = LayoutInflater.from(this).inflate(R.layout.pin_dia,null)
                    view.orderNow.setOnClickListener {
                        if(view.address.text?.isNotEmpty()!! && view.address.text.toString().trim() == pinString){
                            finalOrderPlace()
                            dialog.dismiss()
                        }else{
                            view.address.error = "পিন ঠিক করুন"
                        }
                    }
                    dialog.setContentView(view)
                    dialog.show()
                }else{
                    FancyToast.makeText(this,"ওর্ডার প্লেস করা যায়নি, কিছুক্ষন পরে আবার চেষ্টা করুন",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,false).show()
                }
            }else{
                address.error = "আপনার অ্যাড্রেস লিখুন"
            }
        }
    }

    private fun finalOrderPlace() {
        val orderRef = FirebaseDatabase.getInstance().getReference("pendingOrders")
        val key = orderRef.push().key.toString()
        orderRef.child(key).child("phone").setValue(firebaseAuth.currentUser?.phoneNumber.toString())
        orderRef.child(key).child("name").setValue("$userName  $time")
        orderRef.child(key).child("location").setValue("$lat,$lang")
        val cartItems = cartViewModel.getCartItems(this)
        for(item in cartItems){
            orderRef.child(key).child("products").child(item.id).child("name").setValue(item.name)
            orderRef.child(key).child("products").child(item.id).child("src").setValue(item.src)
            orderRef.child(key).child("products").child(item.id).child("price").setValue(item.price.toString())
            orderRef.child(key).child("products").child(item.id).child("count").setValue(item.count.toString())
        }
        orderRef.child(key).child("address").setValue(address.text.toString())
        FirebaseDatabase.getInstance()
            .getReference("users/"+firebaseAuth.currentUser?.phoneNumber.toString())
            .child("orders")
            .child(key).setValue("1")
            .addOnCompleteListener {
            if(it.isSuccessful){
                cartViewModel.deleteAll(this)
                FancyToast.makeText(this,"ওর্ডার প্লেস করা হয়েছে, শীঘ্রই আপনাকে কল করে কনফার্ম করা হবে",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show()
                val i = Intent(this, HomeActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }else{
                FancyToast.makeText(this,"ওর্ডার প্লেস করা যায়নি, কিছুক্ষন পরে আবার চেষ্টা করুন",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,false).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        airLocation.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = firebaseAuth.currentUser
                if(user!=null){
                    phoneCodeLinear.visibility = View.GONE
                    askName(user)
                }else{
                    phoneCodeLinear.visibility = View.VISIBLE
                    userInfoLinear.visibility = View.GONE
                    orderNow.visibility = View.GONE
                }
            } else {
                phoneCodeLinear.visibility = View.VISIBLE
                userInfoLinear.visibility = View.GONE
                orderNow.visibility = View.GONE
            }
        }
    }
    private fun askName(user: FirebaseUser) {
        val userNameRef: DatabaseReference = databaseReference.child(user.phoneNumber.toString())
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    phoneCodeLinear.visibility = View.GONE
                    userInfoLinear.visibility = View.VISIBLE
                    orderNow.visibility = View.GONE
                }else{
                    phoneCodeLinear.visibility = View.GONE
                    userInfoLinear.visibility = View.GONE
                    orderNow.visibility = View.VISIBLE
                    pinString = dataSnapshot.child("pin").value.toString()
                    userName = dataSnapshot.child("userName").value.toString()
                    user_name.text = "স্বাগতম "+dataSnapshot.child("userName").value.toString()+
                            "\n আপনার ফোন নাম্বারঃ "+user.phoneNumber.toString()
                    change_number.setOnClickListener{
                        it.startAnimation(animation)
                        firebaseAuth.signOut()
                        finish()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("USER SIGN IN", databaseError.message) //Don't ignore errors!
            }
        }
        userNameRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun createFinalOrderBill() : String {
        var s = ""
        val i = cartViewModel.getAllCost(this)
        s += "আপনি মোট "+cartViewModel.getAllCount(this).toString()+ " টি প্রোডাক্ট ওর্ডার করতে চলেছেন"+ "\n\n"
        s += "এগুলোর সর্বোমোট মুল্যঃ  $i টাকা\n\n"
        s += "ডেলিভারি চার্জঃ 30 টাকা\n\n"
        s += "আপনার মোট পরিশোধ করতে হবেঃ "+ (i+30).toString() + " টাকা"
        return s
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults) // ADD THIS LINE INSIDE onRequestPermissionResult
    }
}