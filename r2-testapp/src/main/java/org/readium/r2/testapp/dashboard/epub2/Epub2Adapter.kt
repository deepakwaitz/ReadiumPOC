package org.readium.r2.testapp.dashboard.epub2

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener

class Epub2Adapter(var epub2: List<String>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<Epub2Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Epub2Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Epub2Holder(inflater, parent)
    }

    override fun getItemCount(): Int {

        return  epub2.size
    }

    override fun onBindViewHolder(holder: Epub2Holder, position: Int) {
        var data = epub2[position]
        holder.bind(data)
        holder.itemView.card_view.setOnClickListener {
           Log.e("Click","yes")
            recyclerViewItemClickListener.itemClick(BookType.EPUB2X,position)
        }
    }

}

 class Epub2Holder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(epub2: String) {
        itemView.titleTextView.text = epub2
    }

}
