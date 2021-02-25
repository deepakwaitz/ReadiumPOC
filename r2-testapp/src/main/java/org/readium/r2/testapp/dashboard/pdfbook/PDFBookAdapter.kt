package org.readium.r2.testapp.dashboard.epub2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.android.synthetic.main.item_recycle_sample.view.*

import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.RecyclerViewItemClickListener
import org.readium.r2.testapp.db.Book
import org.readium.r2.testapp.library.LibraryActivity
import java.io.ByteArrayInputStream


class PDFBookAdapter(var context: LibraryActivity, var pdf: ArrayList<Book>, var recyclerViewItemClickListener: RecyclerViewItemClickListener) : RecyclerView.Adapter<PDFViewHolder>() {
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
//        data.cover?.let {
//
//            holder.itemView.coverImageView.setImageBitmap(bitmap)
//        }
        holder.itemView.card_view.setOnClickListener {
            data.fileName?.let { it1 -> recyclerViewItemClickListener.itemClickFile(it1) }
        }
    }
//    fun generateImageFromPdf(pdfUri: Uri?): Bitmap? {
//        val pageNumber = 0
//        val pdfiumCore = PdfiumCore(context)
//        try {
//            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
//            val fd: ParcelFileDescriptor = context.getContentResolver().openFileDescriptor(pdfUri!!, "r")!!
//            val pdfDocument: PdfDocument = pdfiumCore.newDocument(fd)
//            pdfiumCore.openPage(pdfDocument, pageNumber)
//            val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber)
//            val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber)
//            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
//            pdfiumCore.closeDocument(pdfDocument) // important!
//            return bmp;
//        } catch (e: Exception) {
//            //todo with exception
//        }
//    }

}



class PDFViewHolder(inflater: LayoutInflater, parent: ViewGroup):RecyclerView.ViewHolder(
        inflater.inflate(R.layout.item_recycle_pdf,parent,false)) {
    fun bind(pdf: Book) {
        itemView.titleTextView.text = pdf.title
    }
}
