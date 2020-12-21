package com.dubd.magurahut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dubd.magurahut.R
import com.dubd.magurahut.adapters.models.CartItem
import kotlinx.android.synthetic.main.cart_item.view.*


class CartHistoryAdapter(
    private var context: Context,
    private var products: ArrayList<CartItem>
) : RecyclerView.Adapter<CartHistoryAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.img_main
        val textMain: TextView = itemView.mainText
        val priceText2: TextView = itemView.priceText2

        val countText2 : TextView = itemView.productCount2

        val wholePrice : TextView = itemView.wholePrice
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.textMain.text = products[position].name
        holder.priceText2.text = "৳"+products[position].price.toString()
        holder.countText2.text = products[position].count.toString()
        holder.wholePrice.text = "৳ "+(products[position].count * products[position].price).toString()

        Glide.with(context)
            .load(products[position].src)
            .centerCrop()
            .placeholder(R.drawable.place_holder)
            .override(300,300)
            .into(holder.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.cart_item_history_products,parent,false)
        return RecyclerViewHolder(view)
    }
}
