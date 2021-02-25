package org.readium.r2.testapp.dashboard.epub2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.db.Book

class PDFBookAdapter(var pdf: ArrayList<Book>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<PDFViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PDFViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  pdf.size
    }

    override fun onBindViewHolder(holder: PDFViewHolder, position: Int) {
        val data=  pdf[position]
        holder.bind(data)
        holder.itemView.card_view.setOnClickListener {
            data.fileName?.let { it1 -> recyclerViewItemClickListener.itemClickFile(it1) }
        }
    }

}

class PDFViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(pdf: Book) {
        itemView.titleTextView.text = pdf.title
    }
}
