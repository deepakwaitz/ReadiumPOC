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

class OtherBookAdapter(var downloadBook: ArrayList<Book>,var  recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<OtherBookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherBookViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OtherBookViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  downloadBook.size
    }

    override fun onBindViewHolder(holder: OtherBookViewHolder, position: Int) {
        var data = downloadBook[position]
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

class OtherBookViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(downloadBook: Book) {
        itemView.titleTextView.text = downloadBook.title
    }
}
