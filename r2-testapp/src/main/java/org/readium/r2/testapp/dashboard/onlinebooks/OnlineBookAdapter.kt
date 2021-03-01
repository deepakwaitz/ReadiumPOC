package org.readium.r2.testapp.dashboard.epub2

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.dashboard.epub3.OnlineEpubActivity
import org.readium.r2.testapp.library.LibraryActivity

class OnlineBookAdapter(var context: LibraryActivity,var  onlineBook1: List<Int>, var onlineBook: List<String>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<OnlineBookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineBookViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OnlineBookViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  onlineBook.size
    }

    override fun onBindViewHolder(holder: OnlineBookViewHolder, position: Int) {
        var data = onlineBook[position]
        var dataImage = onlineBook1[position]
        holder.bind(data,dataImage)


        holder.itemView.card_view.setOnClickListener {
            var intent= Intent(context, OnlineEpubActivity::class.java)
            intent.putExtra("position",position)
            context.startActivity(intent)
        }
    }

}

class OnlineBookViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {

    fun bind(data: String, onlineBook1: Int) {
        itemView.titleTextView.text = data
        itemView.coverImageView.setImageResource(onlineBook1)
    }

}
