package org.readium.r2.testapp.dashboard

interface RecyclerViewItemClickListener {
    fun itemClick(booktype:BookType,postion:Int)
    fun itemClickFile(filename:String)
}