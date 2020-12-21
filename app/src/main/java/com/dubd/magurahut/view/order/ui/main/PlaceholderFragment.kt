package com.dubd.magurahut.view.order.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubd.magurahut.R
import com.dubd.magurahut.adapters.HistoryAdapter
import com.dubd.magurahut.view.order.OrdersOldActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_main.view.recycler_view

class PlaceholderFragment : Fragment() {

    private lateinit var array: ArrayList<String>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val activity: OrdersOldActivity? = activity as OrdersOldActivity?
        array = activity?.getKeyArray() as ArrayList<String>
        Log.e("ORDERS ARRAY 2",array.toString())
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val recycler = root.recycler_view
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val arrayList = ArrayList<DataSnapshot>()
                for(snap in snapshot.children){
                    if(array.contains(snap.key.toString())){
                        arrayList.add(snap)
                    }
                }
                Log.e("ORDERS ARRAY 3",arrayList.size.toString())
                val recyclerAdapter = context?.let { HistoryAdapter(it,arrayList) }
                recyclerAdapter?.setHasStableIds(true)
                recycler.setHasFixedSize(true)
                recycler.adapter = recyclerAdapter
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                recycler.layoutManager = linearLayoutManager
                recyclerAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }

        })
        return root
    }

    private fun setIndex(index: Int) {
        databaseReference = when (index) {
            1 -> {
                FirebaseDatabase.getInstance().getReference("pendingOrders")
            }
            2 -> {
                FirebaseDatabase.getInstance().getReference("verifiedOrders")
            }
            else -> {
                FirebaseDatabase.getInstance().getReference("deliveredOrders")
            }
        }

    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}