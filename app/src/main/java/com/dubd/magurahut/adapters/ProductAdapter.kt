package com.dubd.magurahut.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dubd.magurahut.R
import com.dubd.magurahut.TinyDB
import com.dubd.magurahut.adapters.models.MainProduct
import com.dubd.magurahut.viewModel.HomeActivityViewModel
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(
    private var context: Context,
    private var products: ArrayList<MainProduct>
) : RecyclerView.Adapter<ProductAdapter.RecyclerViewHolder>() {

    private val homeActivityViewModel : HomeActivityViewModel = HomeActivityViewModel()

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.img_main
        val textMain: TextView = itemView.mainText
        val priceText: TextView = itemView.priceText
        val cardMain: CardView = itemView.cardMainRoot

        val rating : TextView = itemView.rating
        val ratingCount: TextView = itemView.ratingCount

        val addToCart : Button = itemView.addToCart
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.product_item,parent,false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade)

        val tb = TinyDB(context)

        if(products[position].inStock == "true"){
            holder.addToCart.text = "কার্টে যোগ করুন"
            if(products[position].price.trim()==""){
                holder.addToCart.setOnClickListener {
                    FancyToast.makeText(
                        context,
                        "প্রোডাক্টি কার্টে অ্যাড করা যাচ্ছে না",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }else{
                holder.addToCart.setOnClickListener {
                    val stringList = arrayListOf(
                        products[position].name,
                        products[position].price,
                        products[position].image,
                        "1")
                    tb.putListString(products[position].id.toString(),stringList)
                    FancyToast.makeText(context,"কার্টে যোগ করা হয়েছে",
                        FancyToast.LENGTH_SHORT,FancyToast.SUCCESS,false).show()
                }
            }
        } else{
            holder.addToCart.text = "স্টক শেষ"
            holder.addToCart.setOnClickListener {
                FancyToast.makeText(
                    context,
                    "এই প্রোডাক্টের স্টক নেই",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.WARNING,
                    false
                ).show()
            }
        }

        holder.textMain.text = products[position].name
        holder.priceText.text = "৳"+products[position].price
        holder.rating.text = products[position].rating
        holder.ratingCount.text = " ("+products[position].ratingCount.toString()+")"

        holder.cardMain.setOnClickListener {
            it.startAnimation(animation)
            homeActivityViewModel.showProductDialog(context, products[position])
        }

        Glide.with(context)
            .load(products[position].image)
            .centerCrop()
            .placeholder(R.drawable.place_holder)
            .override(300,300)
            .into(holder.imageView)

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
