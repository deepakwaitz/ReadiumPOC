package org.readium.r2.testapp.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.epub2.*

class DashboardActivity : AppCompatActivity(),RecyclerViewItemClickListener {

    var audio = listOf("Audio-1","Audio-2")
    var epub2 = listOf("Epub2x-1","Epub2x-2")
    var epub3 = listOf("Epub3x-1","Epub3x-2")
    var pdf = listOf("Path of Love","Intermediate Economics")
    var comic = listOf("Comic-1","Comic-2")

    lateinit var recyclerViewItemClickListener: RecyclerViewItemClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        recyclerViewItemClickListener=this
        init()
    }

    fun init(){
//        recycler_epub2.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter=Epub2Adapter(epub2,recyclerViewItemClickListener)
//        }

//        recycler_epub3.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter= Epub3Adapter(epub3,recyclerViewItemClickListener)
//        }

//        recycler_comic.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter= ComicBookAdapter(comic,recyclerViewItemClickListener)
//        }

//        recycler_audio.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter= AudioBookAdapter(audio,recyclerViewItemClickListener)
//        }

//        recycler_pdf.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter= PDFBookAdapter(pdf,recyclerViewItemClickListener)
//        }

//        recycler_other.apply {
//            layoutManager = GridLayoutManager(this@DashboardActivity,3)
//            adapter= OtherBookAdapter()
//        }
    }

    override fun itemClick(booktype: BookType, postion: Int) {
        when(booktype){
            BookType.EPUB2X -> Toast.makeText(this,"EPUB2X",Toast.LENGTH_SHORT).show()
            BookType.EPUB3X -> Toast.makeText(this,"EPUB3X",Toast.LENGTH_SHORT).show()
            BookType.COMIC -> Toast.makeText(this,"COMIC",Toast.LENGTH_SHORT).show()
        }
    }

    override fun itemClickFile(filename: String) {
        TODO("Not yet implemented")
    }


}