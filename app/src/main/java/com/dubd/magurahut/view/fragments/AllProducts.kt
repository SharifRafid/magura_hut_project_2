package com.dubd.magurahut.view.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubd.magurahut.R
import com.dubd.magurahut.adapters.ProductAdapter
import com.dubd.magurahut.adapters.models.MainProduct
import com.google.firebase.database.*
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.fragment_all_products.*
import kotlinx.android.synthetic.main.fragment_all_products.view.*
import java.util.*
import kotlin.collections.ArrayList


class AllProducts : Fragment() {

    private var products : ArrayList<MainProduct> = ArrayList()
    private var filterdProducts : ArrayList<MainProduct> = ArrayList()
    private var recyclerAdapter : ProductAdapter? = null
    private lateinit var databaseReference : DatabaseReference
    private var dataLoaded : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Companion.view = inflater.inflate(R.layout.fragment_all_products, container, false)
        Companion.view.avi.visibility = View.VISIBLE
        Companion.view.mainRefresh.setWaveColor(Color.parseColor("#689F38"))
        recyclerAdapter = this.context?.let { ProductAdapter(it, filterdProducts) }
        recyclerAdapter?.setHasStableIds(true)
        Companion.view.recycler_view.setHasFixedSize(true)
        Companion.view.recycler_view.adapter = recyclerAdapter
        val linearLayoutManager = GridLayoutManager(context, 2)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        Companion.view.recycler_view.layoutManager = linearLayoutManager

        Companion.view.mainRefresh.setOnRefreshListener{
            products.shuffle()
            filterdProducts.clear()
            filterdProducts.addAll(products)
            recyclerAdapter?.notifyDataSetChanged()
            Companion.view.mainRefresh.isRefreshing = false
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("data/products")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateList(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(
                    context, "ডাটা সংগ্রহ করা সম্ভব নয়",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR, false
                ).show()
            }

        })
        return Companion.view
    }
    private fun updateList(snapshot: DataSnapshot) {
        for(snapShot in snapshot.children){
            try{
                val product = MainProduct(
                    snapShot.child("id").value.toString().toInt(),
                    snapShot.child("name").value.toString(),
                    snapShot.child("shortDescription").value.toString(),
                    snapShot.child("description").value.toString(),
                    snapShot.child("averageRating").value.toString(),
                    snapShot.child("rating").value.toString().toInt(),
                    snapShot.child("image").value.toString(),
                    snapShot.child("price").value.toString(),
                    snapShot.child("categories").value.toString(),
                    snapShot.child("isInStock").value.toString().trim()
                )
                products.add(product)
            }catch (e: Exception){
                e.printStackTrace()
                FancyToast.makeText(
                    context, "ডাটা সংগ্রহ করা সম্ভব নয়",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR, false
                ).show()
            }
        }
        Companion.view.avi.visibility = View.GONE
        Companion.view.mainRefresh.isRefreshing = false
        products.shuffle()
        filterdProducts.clear()
        filterdProducts.addAll(products)
        recyclerAdapter?.notifyDataSetChanged()
        if(filterdProducts.size==0){
            mainRefresh.visibility = View.GONE
            error.visibility = View.VISIBLE
        }else{
            mainRefresh.visibility = View.VISIBLE
            error.visibility = View.GONE
        }
        dataLoaded = true
    }

    fun filter(keyWord: String, cat: String){
        recycler_view.smoothScrollToPosition(0)
        filterdProducts.clear()
        if(cat.isNotEmpty()){
            if(keyWord.isEmpty()){
                filterdProducts.addAll(products.filter {
                    it.categories.toUpperCase(Locale.ROOT).contains(
                        cat.toUpperCase(
                            Locale.ROOT
                        ).trim()
                    )
                })
            }else{
                val bf = products.filter {it.categories.toUpperCase(Locale.ROOT)
                    .contains(cat.toUpperCase(Locale.ROOT).trim())
                }
                filterdProducts.addAll(bf.filter {
                    it.name.toUpperCase(Locale.ROOT).contains(
                        keyWord.toUpperCase(
                            Locale.ROOT
                        )
                    ) || it.description.toUpperCase(Locale.ROOT)
                        .contains(keyWord.toUpperCase(Locale.ROOT)) || it.categories.toUpperCase(
                        Locale.ROOT
                    ).contains(keyWord.toUpperCase(Locale.ROOT)) && it.categories.toUpperCase(
                        Locale.ROOT
                    )
                        .contains(
                            cat.toUpperCase(
                                Locale.ROOT
                            )
                        )
                })
            }
        }else{
            if(keyWord.isEmpty()){
                filterdProducts.addAll(products)
            }else{
                filterdProducts.addAll(products.filter {
                    it.name.toUpperCase(Locale.ROOT).contains(
                        keyWord.toUpperCase(
                            Locale.ROOT
                        )
                    ) || it.description.toUpperCase(Locale.ROOT)
                        .contains(keyWord.toUpperCase(Locale.ROOT)) || it.categories.toUpperCase(
                        Locale.ROOT
                    ).contains(keyWord.toUpperCase(Locale.ROOT))
                })
            }
        }
        recyclerAdapter?.notifyDataSetChanged()

        if(dataLoaded){
            if(filterdProducts.size==0){
                mainRefresh.visibility = View.GONE
                error.visibility = View.VISIBLE
            }else{
                mainRefresh.visibility = View.VISIBLE
                error.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance(): AllProducts {
            return AllProducts()
        }

        private lateinit var view : View
    }
}