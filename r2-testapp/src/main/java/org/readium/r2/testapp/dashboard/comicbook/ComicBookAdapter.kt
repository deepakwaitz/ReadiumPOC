package org.readium.r2.testapp.dashboard.epub2

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.db.Book
import java.io.ByteArrayInputStream

class ComicBookAdapter(var comic: ArrayList<Book>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<ComicViewHolder>() {
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
        data.cover?.let {
            val arrayInputStream = ByteArrayInputStream(it)
            val bitmap = BitmapFactory.decodeStream(arrayInputStream)
            holder.itemView.coverImageView.setImageBitmap(bitmap)
        }
        holder.itemView.card_view.setOnClickListener {
            data.fileName?.let { it1 -> recyclerViewItemClickListener.itemClickFile(it1) }
        }
    }

}

class ComicViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(comic: Book) {
        itemView.titleTextView.text = comic.title
    }
}
