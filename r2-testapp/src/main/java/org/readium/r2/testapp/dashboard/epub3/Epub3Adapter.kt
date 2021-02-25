package org.readium.r2.testapp.dashboard.epub2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener

class Epub3Adapter(var epub3: List<String>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<Epub3ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Epub3ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return Epub3ViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  epub3.size
    }

    override fun onBindViewHolder(holder: Epub3ViewHolder, position: Int) {
        var data = epub3[position]
        holder.bind(data)
        holder.itemView.card_view.setOnClickListener {
            recyclerViewItemClickListener.itemClick(BookType.EPUB3X,position)
        }
    }

}

class Epub3ViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(epub3: String) {
        itemView.titleTextView.text = epub3
    }
}
