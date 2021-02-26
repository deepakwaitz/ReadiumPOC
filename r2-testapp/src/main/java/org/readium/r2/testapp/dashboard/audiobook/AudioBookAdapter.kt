package org.readium.r2.testapp.dashboard.epub2

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recycle_sample.view.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.audiobook.OnlineAudioActivity
import org.readium.r2.testapp.library.LibraryActivity
import org.readium.r2.testapp.opds.OnlineEbookCatalogActivity

class AudioBookAdapter(var context: LibraryActivity, var audio: List<String>) : RecyclerView.Adapter<AudioViewHolder>() {
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

            var intent=Intent(context,OnlineAudioActivity::class.java)
            intent.putExtra("position",position)
            context.startActivity(intent)
        }
    }

}

class AudioViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_audio,parent,false)) {
    fun bind(audio: String) {
        itemView.titleTextView.text = audio
    }
}
