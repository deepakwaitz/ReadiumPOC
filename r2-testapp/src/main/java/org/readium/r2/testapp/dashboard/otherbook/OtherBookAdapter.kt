package org.readium.r2.testapp.dashboard.epub2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.readium.r2.testapp.R

class OtherBookAdapter() : RecyclerView.Adapter<OtherBookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherBookViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OtherBookViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  3
    }

    override fun onBindViewHolder(holder: OtherBookViewHolder, position: Int) {
//        val userList: UserList = list[position]
//
//        holder.bind(userList)
    }

}

class OtherBookViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {

}
