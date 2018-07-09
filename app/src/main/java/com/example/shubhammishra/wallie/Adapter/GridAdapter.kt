package com.example.shubhammishra.wallie.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.shubhammishra.wallie.GridPojo
import com.example.shubhammishra.wallie.R
import kotlinx.android.synthetic.main.list_view_wallie.view.*

class GridAdapter(val wallArray:ArrayList<GridPojo>,val NextActivity:(gridPojo:GridPojo)->Unit):RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val lf=parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return GridViewHolder(lf.inflate(R.layout.list_view_wallie,parent,false))
    }

    override fun getItemCount(): Int =wallArray.size

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.itemView.tvLikes.text=wallArray[position].likes.toString()
        holder.itemView.tvUsers.text="Views: "+wallArray[position].users.toString()
        Glide.with(holder.itemView.context).load(wallArray[position].thumb)
                .thumbnail(Glide.with(holder.itemView.context).load(R.drawable.backload))
                .fitCenter()
                .crossFade()
                .into(holder.itemView.ivImage);
        // Picasso.get().load("${wallArray[position].thumb}").placeholder(R.drawable.placeholder).into(holder.itemView.ivImage)
        holder.itemView.setOnClickListener({
            NextActivity(wallArray[position])
        })
    }

    class GridViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    }
}