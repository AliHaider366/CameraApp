package com.example.cameraapptestcode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter(var items: List<String>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


//    fun updateList(list: MutableList<String>) {
//        val diffResult = DiffUtil.calculateDiff(DiffUtil(items, list))
//        items = list
//        diffResult.dispatchUpdatesTo(this)
//    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(holder.itemView.context).load(item.toUri().toString()).into(holder.imageView)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.findViewById(R.id.imageView)
    }
}