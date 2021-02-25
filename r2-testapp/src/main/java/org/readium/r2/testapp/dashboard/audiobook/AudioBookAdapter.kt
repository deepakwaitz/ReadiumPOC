package org.readium.r2.testapp.dashboard.epub2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.BookType
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener

class AudioBookAdapter(var audio: List<String>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<AudioViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AudioViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return  audio.size
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
       val data=  audio[position]
       holder.bind(data)
        holder.itemView.card_view.setOnClickListener {
            recyclerViewItemClickListener.itemClick(BookType.COMIC,position)
        }
    }

}

class AudioViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_sample,parent,false)) {
    fun bind(audio: String) {
        itemView.titleTextView.text = audio
    }
}
