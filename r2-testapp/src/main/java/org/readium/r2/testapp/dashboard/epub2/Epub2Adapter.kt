package org.readium.r2.testapp.dashboard.epub2

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.db.Book
import java.io.ByteArrayInputStream

class Epub2Adapter(var epub2: ArrayList<Book>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<Epub2Holder>() {
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
        data.cover?.let {
            val arrayInputStream = ByteArrayInputStream(it)
            val bitmap = BitmapFactory.decodeStream(arrayInputStream)
            holder.itemView.coverImageView.setImageBitmap(bitmap)
        }
        holder.itemView.card_view.setOnClickListener {
           Log.e("Click","yes")
            data.fileName?.let { it1 -> recyclerViewItemClickListener.itemClickFile(it1) }
        }
    }

}

 class Epub2Holder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(epub2: Book) {
        itemView.titleTextView.text = epub2.title
    }

}
