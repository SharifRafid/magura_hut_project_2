package com.dubd.magurahut.adapters

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.dubd.magurahut.R
import com.dubd.magurahut.TinyDB
import com.dubd.magurahut.adapters.models.CartItem
import com.dubd.magurahut.view.main.MyInterface
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import kotlinx.android.synthetic.main.cart_item.view.*


class CartAdapter(
    listData: ArrayList<String>,
    private var context: Context,
    private var products: ArrayList<CartItem>,
    listener: MyInterface?
) : DragDropSwipeAdapter<String, CartAdapter.ViewHolder>(listData) {

    private var listener: MyInterface? = null

    init{
        this.listener = listener
    }

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val imageView: ImageView = itemView.img_main
        val textMain: TextView = itemView.mainText
        val priceText: TextView = itemView.priceText
        val priceText2: TextView = itemView.priceText2

        val imgAdd : ImageView = itemView.imgAdd
        val imgDlt : ImageView = itemView.imgDlt
        val imgMinus : ImageView= itemView.imgMinus
        val countText : TextView = itemView.productCount
        val countText2 : TextView = itemView.productCount2

        val wholePrice : TextView = itemView.wholePrice

        val cardMain : CardView = itemView.cardMainRoot
    }

    override fun getViewHolder(itemLayout: View) = ViewHolder(itemLayout)

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(item: String, holder: ViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade)

        holder.textMain.text = products[position].name
        holder.priceText.text = "৳"+products[position].price.toString()
        holder.priceText2.text = "৳"+products[position].price.toString()
        holder.countText.text = products[position].count.toString()
        holder.countText2.text = products[position].count.toString()
        holder.wholePrice.text = "৳ "+(products[position].count * products[position].price).toString()

        holder.imgAdd.setOnClickListener {
            it.startAnimation(animation)
            var count = holder.countText.text.toString().toInt()
            count ++
            saveData(position, count)
            holder.countText.text = count.toString()
            holder.countText2.text = count.toString()
            holder.wholePrice.text = "৳ "+(count * products[position].price).toString()
            products[position].count = count
            listener?.changeWholePrice()
        }

        holder.imgMinus.setOnClickListener {
            it.startAnimation(animation)
            var count = holder.countText.text.toString().toInt()
            if(count!=1){
                count --
                saveData(position, count)
                holder.countText.text = count.toString()
                holder.countText2.text = count.toString()
                products[position].count = count
                listener?.changeWholePrice()
                holder.wholePrice.text = "৳ "+(count * products[position].price).toString()
            }
        }
        holder.imgDlt.setOnClickListener {
            it.startAnimation(animation)
            listener?.removeData(position)
        }

        Glide.with(context)
            .load(products[position].src)
            .centerCrop()
            .placeholder(R.drawable.place_holder)
            .override(300,300)
            .into(holder.imageView)
    }

    override fun getViewToTouchToStartDraggingItem(item: String, viewHolder: ViewHolder, position: Int): View? {
        // We return the view holder's view on which the user has to touch to drag the item
        return viewHolder.imageView
    }

    private fun saveData(position: Int, count: Int) {
        val tb = TinyDB(context)

        val stringList = arrayListOf(
            products[position].name,
            products[position].price.toString(),
            products[position].src,
            count.toString())

        tb.putListString(products[position].id,stringList)
    }
}
