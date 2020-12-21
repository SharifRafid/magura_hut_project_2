package com.dubd.magurahut.view.main

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubd.magurahut.R
import com.dubd.magurahut.TinyDB
import com.dubd.magurahut.adapters.CartAdapter
import com.dubd.magurahut.adapters.models.CartItem
import com.dubd.magurahut.view.order.OrderActivity
import com.dubd.magurahut.view.order.OrdersOldActivity
import com.dubd.magurahut.viewModel.CartViewModel
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_cart.*
import java.io.IOException
import kotlin.collections.ArrayList


class CartActivity : AppCompatActivity() , MyInterface{

    private val cartViewModel: CartViewModel = CartViewModel()
    private lateinit var cartAdapter : CartAdapter
    private lateinit var cartItems : ArrayList<CartItem>
    private lateinit var setData : ArrayList<String>
    private lateinit var animation: Animation
    private var internetAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setTheme(R.style.MyTheme)

        internetAvailable = checkInternet()

        cartItems = cartViewModel.getCartItems(this)
        if(cartItems.size!=0){
            setData = ArrayList()
            for(cartItem in cartItems){
                setData.add(cartItem.name)
            }
            createRecyclerAdapter()
            wholePrice.text = cartViewModel.getAllCost(this).toString()
        }else{
            wholePrice.text = "0"
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.fade)

        addToCart.setOnClickListener {
            if(internetAvailable){
                if(wholePrice.text.toString() != "0"){
                    startActivity(Intent(this, OrderActivity::class.java))
                }
            }else{
                showDia()
            }
        }

        fabMain.setOnClickListener {
            if(internetAvailable){
                if(FirebaseAuth.getInstance().currentUser!=null){
                    startActivity(Intent(this, OrdersOldActivity::class.java))
                }else{
                    FancyToast.makeText(this,"লগ ইন করুন",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,false).show()
                }
            }else{
                showDia()
            }
        }

        backImage.setOnClickListener {
            it.startAnimation(animation)
            finish()
        }

        deleteImage.setOnClickListener {
            it.startAnimation(animation)
            val mDialog = MaterialDialog.Builder(this)
                .setTitle("আপনি কি নিশ্চিত ?")
                .setMessage("আপনি আপনার কার্টের সকল প্রোডাক্ট ডিলেট করতে যাচ্ছেন")
                .setCancelable(false)
                .setPositiveButton(
                    "ডিলেট করুন!", R.drawable.ic_delete
                ) { diaInt, _ ->
                    TinyDB(this).clear()
                    diaInt.dismiss()
                    finish()
                }
                .setNegativeButton(
                    "না। থাক।",
                    R.drawable.ic_clear
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .build()

            mDialog.show()
        }
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
                if (cartViewModel.callPermissionCheck(this, this)) {
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


    private fun createRecyclerAdapter() {
        cartAdapter = CartAdapter(setData, this, cartItems, this)
        recycler_view.adapter = cartAdapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.setHasFixedSize(true)
        cartAdapter.notifyDataSetChanged()

        recycler_view.swipeListener = onItemSwipeListener
        recycler_view.dragListener = onItemDragListener
        recycler_view.scrollListener = onListScrollListener

        recycler_view.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)
        recycler_view.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<String> {
        override fun onItemSwiped(
            position: Int,
            direction: OnItemSwipeListener.SwipeDirection,
            item: String
        ): Boolean {
            // Handle action of item swiped
            // Return false to indicate that the swiped item should be removed from the adapter's data set (default behaviour)
            // Return true to stop the swiped item from being automatically removed from the adapter's data set (in this case, it will be your responsibility to manually update the data set as necessary)
            removeData(position)
            return false
        }
    }


    private val onItemDragListener = object : OnItemDragListener<String> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
            // Handle action of item being dragged from one position to another
        }

        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {
            // Handle action of item dropped
        }
    }

    private val onListScrollListener = object : OnListScrollListener {
        override fun onListScrollStateChanged(scrollState: OnListScrollListener.ScrollState) {
            // Handle change on list scroll state
        }

        override fun onListScrolled(
            scrollDirection: OnListScrollListener.ScrollDirection,
            distance: Int
        ) {
            // Handle scrolling
        }
    }

    override fun changeWholePrice() {
        wholePrice.text = cartViewModel.getAllCost(this).toString()
    }

    override fun removeData(position: Int){
        cartViewModel.deleteCartItem(this@CartActivity, cartItems[position].id)
        cartItems.removeAt(position)
        cartAdapter.notifyItemRemoved(position)
        cartAdapter.notifyDataSetChanged()
        changeWholePrice()
    }
}

interface MyInterface {
    fun changeWholePrice()
    fun removeData(position: Int)
}