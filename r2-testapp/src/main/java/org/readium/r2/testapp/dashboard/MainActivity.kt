package org.readium.r2.testapp.dashboard

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.design.textInputLayout
import org.readium.r2.lcp.LcpService
import org.readium.r2.shared.Injectable
import org.readium.r2.shared.extensions.extension
import org.readium.r2.shared.extensions.mediaType
import org.readium.r2.shared.extensions.toPng
import org.readium.r2.shared.extensions.tryOrNull
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.asset.FileAsset
import org.readium.r2.shared.publication.asset.PublicationAsset
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.publication.services.isRestricted
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.flatMap
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.streamer.Streamer
import org.readium.r2.streamer.server.Server
import org.readium.r2.testapp.BuildConfig
import org.readium.r2.testapp.R
import org.readium.r2.testapp.dashboard.epub2.*
import org.readium.r2.testapp.db.Book
import org.readium.r2.testapp.db.BooksDatabase
import org.readium.r2.testapp.db.books
import org.readium.r2.testapp.library.BooksAdapter
import org.readium.r2.testapp.permissions.PermissionHelper
import org.readium.r2.testapp.permissions.Permissions
import org.readium.r2.testapp.utils.ContentResolverUtil
import org.readium.r2.testapp.utils.NavigatorContract
import org.readium.r2.testapp.utils.extensions.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(),RecyclerViewItemClickListener, CoroutineScope {

    /**
     * Context of this scope.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var server: Server
    private var localPort: Int = 0

    private lateinit var booksAdapter: BooksAdapter
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var permissions: Permissions
    private lateinit var preferences: SharedPreferences
    private lateinit var R2DIRECTORY: String

    private lateinit var database: BooksDatabase

    private lateinit var streamer: Streamer
    private lateinit var lcpService: Try<LcpService, Exception>

    // private lateinit var catalogView: androidx.recyclerview.widget.RecyclerView
    private lateinit var alertDialog: AlertDialog
    private lateinit var documentPickerLauncher: ActivityResultLauncher<String>
    private lateinit var navigatorLauncher: ActivityResultLauncher<NavigatorContract.Input>


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
      //  init()

        preferences = getSharedPreferences("org.readium.r2.settings", Context.MODE_PRIVATE)

        lcpService = LcpService(this)
                ?.let { Try.success(it) }
                ?: Try.failure(Exception("liblcp is missing on the classpath"))

        streamer = Streamer(this,
                contentProtections = listOfNotNull(
                        lcpService.getOrNull()?.contentProtection()
                )
        )

        val s = ServerSocket(if (BuildConfig.DEBUG) 8080 else 0)
        s.localPort
        s.close()

        localPort = s.localPort
        server = Server(localPort, applicationContext)

        val properties = Properties()
        val inputStream = this.assets.open("configs/config.properties")
        properties.load(inputStream)
        val useExternalFileDir = properties.getProperty("useExternalFileDir", "false")!!.toBoolean()

        R2DIRECTORY = if (useExternalFileDir) {
            this.getExternalFilesDir(null)?.path + "/"
        } else {
            this.filesDir.path + "/"
        }

        permissions = Permissions(this)
        permissionHelper = PermissionHelper(this, permissions)

        database = BooksDatabase(this)
        books = database.books.list()
        if(books.size==7){
            Log.e("BookCheck",""+ books.size)
            launch {
                bookList()
            }

        }

        Log.e("BookSize",""+ books.size)
        launch {
            if(books.size>0){
                bookList()
            }

        }


        //  booksAdapter = BooksAdapter(this, books, this)

        documentPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { importPublicationFromUri(it) }
        }

        navigatorLauncher = registerForActivityResult(NavigatorContract()) { pubData: NavigatorContract.Output? ->
            if (pubData == null)
                return@registerForActivityResult

            tryOrNull { pubData.publication.close() }
            Timber.d("Publication closed")
            if (pubData.deleteOnResult)
                tryOrNull { pubData.file.delete() }
        }

        intent.data?.let { importPublicationFromUri(it) }

//        coordinatorLayout {
//            lparams {
//                topMargin = dip(8)
//                bottomMargin = dip(8)
//                padding = dip(0)
//                width = matchParent
//                height = matchParent
//            }
//
////            catalogView = recyclerView {
////                layoutManager = GridAutoFitLayoutManager(this@TestActivity, 120)
////                adapter = booksAdapter
////
////                lparams {
////                    elevation = 2F
////                    width = matchParent
////                }
////
////                addItemDecoration(VerticalSpaceItemDecoration(10))
////
////            }
//
////            floatingActionButton {
////                imageResource = R.drawable.icon_plus_white
////                contentDescription = context.getString(R.string.floating_button_add_book)
////
////                onClick {
////
////                    alertDialog = alert(Appcompat, context.getString(R.string.add_publication_to_library)) {
////                        customView {
////                            verticalLayout {
////                                lparams {
////                                    bottomPadding = dip(16)
////                                }
////                                button {
////                                    text = context.getString(R.string.select_from_your_device)
////                                    onClick {
////                                        alertDialog.dismiss()
////                                        documentPickerLauncher.launch("*/*")
////                                    }
////                                }
////                                button {
////                                    text = context.getString(R.string.download_from_url)
////                                    onClick {
////                                        alertDialog.dismiss()
////                                        showDownloadFromUrlAlert()
////                                    }
////                                }
////                            }
////                        }
////                    }.show()
////                }
////            }.lparams {
////                gravity = Gravity.END or Gravity.BOTTOM
////                margin = dip(16)
////            }
//        }

    }
    override fun onStart() {
        super.onStart()

        startServer()

        permissionHelper.storagePermission {
            if (books.isEmpty()) {
                if (!preferences.contains("samples")) {
                    val dir = File(R2DIRECTORY)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    launch {
                        copySamplesFromAssetsToStorage()
                    }
                    preferences.edit().putBoolean("samples", true).apply()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
      //  booksAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        //TODO not sure if this is needed
        stopServer()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startServer() {
        if (!server.isAlive) {
            try {
                server.start()
            } catch (e: IOException) {
                // do nothing
                if (BuildConfig.DEBUG) Timber.e(e)
            }
            if (server.isAlive) {
//                // Add your own resources here
//                server.loadCustomResource(assets.open("scripts/test.js"), "test.js")
//                server.loadCustomResource(assets.open("styles/test.css"), "test.css")
//                server.loadCustomFont(assets.open("fonts/test.otf"), applicationContext, "test.otf")

                server.loadCustomResource(assets.open("Search/mark.js"), "mark.js", Injectable.Script)
                server.loadCustomResource(assets.open("Search/search.js"), "search.js", Injectable.Script)
                server.loadCustomResource(assets.open("Search/mark.css"), "mark.css", Injectable.Style)

               isServerStarted = true
            }
        }
    }

    private fun stopServer() {
        if (server.isAlive) {
            server.stop()
            isServerStarted = false
        }
    }

    private suspend fun copySamplesFromAssetsToStorage() = withContext(Dispatchers.IO) {
        val samples = assets.list("Samples")?.filterNotNull().orEmpty()

        for (element in samples) {
            val file = assets.open("Samples/$element").copyToTempFile()
            if (file != null)
                importPublication(file)
            else if (org.jetbrains.anko.design.BuildConfig.DEBUG)
                error("Unable to load sample into the library")
        }
    }

    private fun showDownloadFromUrlAlert() {
        var editTextHref: EditText? = null
        alert(Appcompat, "Add a publication from URL") {

            customView {
                verticalLayout {
                    textInputLayout {
                        padding = dip(10)
                        editTextHref = editText {
                            hint = "URL"
                            contentDescription = "Enter A URL"
                        }
                    }
                }
            }
            positiveButton("Add") { }
            negativeButton("Cancel") { }

        }.build().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnShowListener {
                val b = getButton(AlertDialog.BUTTON_POSITIVE)
                b.setOnClickListener {
                    if (TextUtils.isEmpty(editTextHref!!.text)) {
                        editTextHref!!.error = "Please Enter A URL."
                        editTextHref!!.requestFocus()
                    } else if (!URLUtil.isValidUrl(editTextHref!!.text.toString())) {
                        editTextHref!!.error = "Please Enter A Valid URL."
                        editTextHref!!.requestFocus()
                    } else {
                        val url = tryOrNull { URL(editTextHref?.text.toString()) }
                                ?: return@setOnClickListener

                        launch {
                            val progress =
                                    blockingProgressDialog(getString(R.string.progress_wait_while_downloading_book))
                                            .apply { show() }

                            val downloadedFile = url.copyToTempFile() ?: return@launch
                            importPublication(downloadedFile, sourceUrl = url.toString(), progress = progress)
                        }
                    }
                }
            }

        }.show()
    }

    private fun importPublicationFromUri(uri: Uri) {

        launch {
            val progress = blockingProgressDialog(getString(R.string.progress_wait_while_downloading_book))
                    .apply { show() }

            uri.copyToTempFile()
                    ?.let { importPublication(it, sourceUrl = uri.toString(), progress = progress) }
                    ?: progress.dismiss()
        }
    }

    private suspend fun importPublication(sourceFile: File, sourceUrl: String? = null, progress: ProgressDialog? = null) {
        val foreground = progress != null
        val sourceMediaType = sourceFile.mediaType()

        val publicationAsset: FileAsset =
                if (sourceMediaType != MediaType.LCP_LICENSE_DOCUMENT)
                    FileAsset(sourceFile, sourceMediaType)
                else {
                    lcpService
                            .flatMap { it.acquirePublication(sourceFile) }
                            .fold(
                                    {
                                        val mediaType = MediaType.of(fileExtension = File(it.suggestedFilename).extension)
                                        FileAsset(it.localFile, mediaType)
                                    },
                                    {
                                        tryOrNull { sourceFile.delete() }
                                        Timber.d(it)
                                        progress?.dismiss()
                                        //    if (foreground) catalogView.longSnackbar("fulfillment error: ${it.message}")
                                        return
                                    }
                            )
                }

        val mediaType = publicationAsset.mediaType()
        val fileName = "${UUID.randomUUID()}.${mediaType.fileExtension}"
        val libraryAsset = FileAsset(File(R2DIRECTORY + fileName), mediaType)

        try {
            publicationAsset.file.moveTo(libraryAsset.file)
        } catch (e: Exception) {
            Timber.d(e)
            tryOrNull { publicationAsset.file.delete() }
            progress?.dismiss()
            // if (foreground) catalogView.longSnackbar("unable to move publication into the library")
            return
        }

        val extension = libraryAsset.let {
            it.mediaType().fileExtension ?: it.file.extension
        }

        val isRwpm = libraryAsset.mediaType().isRwpm

        val bddHref =
                if (!isRwpm)
                    libraryAsset.file.path
                else
                    sourceUrl ?: run {
                        Timber.e("Trying to add a RWPM to the database from a file without sourceUrl.")
                        progress?.dismiss()
                        return
                    }

        streamer.open(libraryAsset, allowUserInteraction = false, sender = this@MainActivity)
                .onSuccess {
                    addPublicationToDatabase(bddHref, extension, it).let {success ->
                        progress?.dismiss()
                        val msg =
                                if (success){
                                    Log.e("BookSize",""+ books.size)
                                    if(books.size==7){
                                        Log.e("BookCheck",""+ books.size)
                                            bookList()
                                    }
                                    "publication added to your library"

                                }

                                else
                                    "unable to add publication to the database"
                        if (foreground)
                        // catalogView.longSnackbar(msg)
                        else
                            Timber.d(msg)
                        if (success && isRwpm)
                            tryOrNull { libraryAsset.file.delete() }
                    }
                }
                .onFailure {
                    tryOrNull { libraryAsset.file.delete() }
                    Timber.d(it)
                    progress?.dismiss()
                    if (foreground) presentOpeningException(it)
                }
    }

    private suspend fun addPublicationToDatabase(href: String, extension: String, publication: Publication): Boolean {
        val publicationIdentifier = publication.metadata.identifier ?: ""
        val author = publication.metadata.authorName
        val cover = publication.cover()?.toPng()

        val book = Book(
                title = publication.metadata.title,
                author = author,
                href = href,
                identifier = publicationIdentifier,
                cover = cover,
                ext = ".$extension",
                progression = "{}"
        )

        return addBookToDatabase(book)
    }

    private suspend fun addBookToDatabase(book: Book, alertDuplicates: Boolean = true): Boolean {
        database.books.insert(book, allowDuplicates = !alertDuplicates)?.let { id ->
            book.id = id
            books.add(0, book)
            withContext(Dispatchers.Main) {
                Log.e("Success","s")
             // booksAdapter.notifyDataSetChanged()
            }
            return true
        }

        return if (alertDuplicates && confirmAddDuplicateBook(book))
            addBookToDatabase(book, alertDuplicates = false)
        else
            false
    }

    private suspend fun URL.copyToTempFile(): File? = tryOrNull {
        val filename = UUID.randomUUID().toString()
        val path = "$R2DIRECTORY$filename.$extension"
        download(path)
    }

    private suspend fun Uri.copyToTempFile(): File? = tryOrNull {
        val filename = UUID.randomUUID().toString()
        val mediaType = MediaType.ofUri(this, contentResolver)
        val path = "$R2DIRECTORY$filename.${mediaType?.fileExtension ?: "tmp"}"
        ContentResolverUtil.getContentInputStream(this@MainActivity, this, path)
        return File(path)
    }

    private suspend fun InputStream.copyToTempFile(): File? = tryOrNull {
        val filename = UUID.randomUUID().toString()
        File(R2DIRECTORY + filename)
                .also { toFile(it.path) }
    }

    private suspend fun confirmAddDuplicateBook(book: Book): Boolean = suspendCoroutine { cont ->
        alert(Appcompat, "Publication already exists") {
            positiveButton("Add anyway") {
                it.dismiss()
                cont.resume(true)
            }
            negativeButton("Cancel") {
                it.dismiss()
                cont.resume(false)
            }
        }.build().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    private fun prepareToServe(publication: Publication, asset: PublicationAsset) {
        val key = publication.metadata.identifier ?: publication.metadata.title
        preferences.edit().putString("$key-publicationPort", localPort.toString()).apply()
        val userProperties = applicationContext.filesDir.path + "/" + Injectable.Style.rawValue + "/UserProperties.json"
        server.addEpub(publication, null, "/${asset.name}", userProperties)
    }

    private fun presentOpeningException(error: Publication.OpeningException) {
        //   catalogView.longSnackbar(error.getUserMessage(this))
    }

    class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView,
                                    state: androidx.recyclerview.widget.RecyclerView.State) {
            outRect.bottom = verticalSpaceHeight
        }
    }
    companion object {

        var isServerStarted = false
            private set

    }
    fun init(){
//        recycler_epub2.apply {
//            layoutManager = GridLayoutManager(this@MainActivity,3)
//            adapter= Epub2Adapter(epub2,recyclerViewItemClickListener)
//        }

//        recycler_epub3.apply {
//            layoutManager = GridLayoutManager(this@MainActivity,3)
//            adapter= Epub3Adapter(epub3,recyclerViewItemClickListener)
//        }

//        recycler_comic.apply {
//            layoutManager = GridLayoutManager(this@MainActivity,3)
//            adapter= ComicBookAdapter(comic,recyclerViewItemClickListener)
//        }

//        recycler_audio.apply {
//            layoutManager = GridLayoutManager(this@MainActivity,3)
//            adapter= AudioBookAdapter(audio,recyclerViewItemClickListener)
//        }
//
//
//        recycler_other.apply {
//            layoutManager = GridLayoutManager(this@MainActivity,3)
//            adapter= OtherBookAdapter()
//        }
    }

    suspend fun bookList(){
        books = database.books.list()
        var pdfBook = ArrayList<Book>()
        var epubBook = ArrayList<Book>()
        var epub2Book = ArrayList<Book>()
        var comicBook = ArrayList<Book>()
        for(i in books.indices){
            when(books.get(i).ext){
                ".epub"->{
                    if(books.get(i).title!!.startsWith("Price")){
                        epub2Book.add(books.get(i))
                    }else{
                        epubBook.add(books.get(i))
                    }
                }
                ".pdf"-> pdfBook.add(books.get(i))
                ".cbz"-> comicBook.add(books.get(i))
            }
        }
        withContext(Dispatchers.Main) {

            recycler_epub2.apply {
                layoutManager = GridLayoutManager(this@MainActivity,3)
                adapter= Epub2Adapter(epub2Book,recyclerViewItemClickListener)
                (adapter as Epub2Adapter).notifyDataSetChanged()
            }

            recycler_epub3.apply {
                layoutManager = GridLayoutManager(this@MainActivity,3)
                adapter= Epub3Adapter(epubBook,recyclerViewItemClickListener)
                (adapter as Epub3Adapter).notifyDataSetChanged()
            }

//            recycler_pdf.apply {
//                layoutManager = GridLayoutManager(this@MainActivity,3)
//                adapter= PDFBookAdapter(this@LibraryActivity, pdfBook, recyclerViewItemClickListener)
//                (adapter as PDFBookAdapter).notifyDataSetChanged()
//            }

            recycler_comic.apply {
                layoutManager = GridLayoutManager(this@MainActivity,3)
                adapter= ComicBookAdapter(comicBook,recyclerViewItemClickListener)
                (adapter as ComicBookAdapter).notifyDataSetChanged()
            }
        }

        Log.e("pdfBoook",""+pdfBook.size)
    }
    override fun itemClick(booktype: BookType, position: Int) {

    }

    override fun itemClickFile(filename: String) {
        val progress = blockingProgressDialog(getString(R.string.progress_wait_while_preparing_book))
        /*
        FIXME: if the progress dialog were shown, the LCP popup window would not be accessible to the user.
        progress.show()
         */
        progress.dismiss()

        launch {
            val booksDB = BooksDatabase(this@MainActivity)
             var book:Book?=null
            for(i in books.indices){
                if(books[i].fileName==filename){
                   book = books[i]
                }
            }
            val remoteAsset: FileAsset? = tryOrNull { URL(book!!.href).copyToTempFile()?.let { FileAsset(it) } }
            val mediaType = MediaType.of(fileExtension = book!!.ext.removePrefix("."))
            val asset = remoteAsset // remote file
                    ?: FileAsset(File(book.href), mediaType = mediaType) // local file

            streamer.open(asset, allowUserInteraction = true, sender = this@MainActivity)
                    .onFailure {
                        Timber.d(it)
                        progress.dismiss()
                        presentOpeningException(it)
                    }
                    .onSuccess { it ->
                        if (it.isRestricted) {
                            progress.dismiss()
//                            it.protectionError?.let { error ->
//                                Timber.d(error)
//                                catalogView.longSnackbar(error.getUserMessage(this@LibraryActivity))
//                            }
                        } else {
                            prepareToServe(it, asset)
                            progress.dismiss()
                            navigatorLauncher.launch(
                                    NavigatorContract.Input(
                                            file = asset.file,
                                            mediaType = mediaType,
                                            publication = it,
                                            bookId = book!!.id,
                                            initialLocator = book.id?.let { id -> booksDB.books.currentLocator(id) },
                                            deleteOnResult = remoteAsset != null,
                                            baseUrl = Publication.localBaseUrlOf(asset.name, localPort)
                                    )
                            )
                        }
                    }
        }
    }
}