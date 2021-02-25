package org.readium.r2.testapp.dashboard.epub2

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.db.Book
import java.io.ByteArrayInputStream

class Epub3Adapter(var epub3: ArrayList<Book>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<Epub3ViewHolder>() {
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

class Epub3ViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(epub3: Book) {
        itemView.titleTextView.text = epub3.title
    }
}
