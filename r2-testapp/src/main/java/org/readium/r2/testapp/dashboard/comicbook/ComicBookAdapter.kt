package org.readium.r2.testapp.dashboard.epub2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener

class ComicBookAdapter(var comic: List<String>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<ComicViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ComicViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  comic.size
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        var data = comic[position]
        holder.bind(data)
        holder.itemView.card_view.setOnClickListener {
            recyclerViewItemClickListener.itemClick(BookType.COMIC,position)
        }
    }

}

class ComicViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(comic: String) {
        itemView.titleTextView.text = comic
    }
}
