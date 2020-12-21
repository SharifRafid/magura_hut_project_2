package com.dubd.magurahut.adapters

import android.app.Dialog
import android.content.Context
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dubd.magurahut.R
import com.dubd.magurahut.adapters.models.CartItem
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.dialog_history_products.view.*
import kotlinx.android.synthetic.main.product_item.view.*

class HistoryAdapter(
    private var context: Context,
    private var arrayList: ArrayList<DataSnapshot>
) : RecyclerView.Adapter<HistoryAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.mainText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view : View = LayoutInflater.from(context).inflate(
            R.layout.custom_order_history,
            parent,
            false
        )
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade)

        val name = arrayList[position].child("name").value.toString()
        holder.textView.text = name

        val items = ArrayList<CartItem>()
        var price = 0
        for(product in arrayList[position].child("products").children){
            price += (product.child("price").value.toString().trim().toInt()*product.child("count").value.toString().trim().toInt())
            items.add(
                CartItem(
                    product.key.toString(),
                    product.child("name").value.toString(),
                    product.child("src").value.toString(),
                    product.child("price").value.toString().trim().toInt(),
                    product.child("count").value.toString().trim().toInt()
                )
            )
        }

        holder.textView.setOnClickListener {
            it.startAnimation(animation)
            val dialog = Dialog(context)
            val dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_history_products,
                null
            )
            dialogView.nameTxt.text = "Title : $name"
            dialogView.price.text = "Price : $price"
            val adapter = CartHistoryAdapter(context, items)
            dialogView.recyclerMain.adapter = adapter
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            dialogView.recyclerMain.layoutManager = linearLayoutManager
            dialogView.recyclerMain.setHasFixedSize(true)
            adapter.notifyDataSetChanged()
            dialog.setContentView(dialogView)
            dialog.show()
            val window: Window? = dialog.window
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
