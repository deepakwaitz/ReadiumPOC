/*
 * Module: r2-testapp-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. European Digital Reading Lab. All rights reserved.
 * Licensed to the Readium Foundation under one or more contributor license agreements.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

@file:Suppress("DEPRECATION")

package org.readium.r2.testapp.opds

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.ListView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commonsware.cwac.merge.MergeAdapter
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.filter_row.view.*
import kotlinx.android.synthetic.main.section_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.Publication
import org.readium.r2.shared.opds.Facet
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.opds.numberOfItems
import org.readium.r2.testapp.BuildConfig.DEBUG
import org.readium.r2.testapp.R
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import kotlin.coroutines.CoroutineContext

const val MAX_PROGRESS = 100
class OPDSCatalogActivity : AppCompatActivity(), CoroutineScope {
    /**
     * Context of this scope.
     */
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var facets: MutableList<Facet>
    private var parsePromise: Promise<ParseData, Exception>? = null
    private var opdsModel: OPDSModel? = null
    private var showFacetMenu = false
    private var facetPopup: PopupWindow? = null
    private lateinit var progress: ProgressDialog
    private var lastVisibleItemPosition: Int = 0
    private var loadTimeStart: Long = 0
    private var loadTimeEnd: Long = 0

    private lateinit var scrollListener: RecyclerView.OnScrollListener
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private var currentPublicationsList: MutableList<Publication> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_webview)
        supportActionBar?.hide()
        //progress = indeterminateProgressDialog(getString(R.string.progress_wait_while_loading_feed))

        opdsModel = intent.getSerializableExtra("opdsModel") as? OPDSModel
        initWebView()
        loadUrl()
        opdsModel?.href.let {
            //progress.show()
            try {
                parsePromise = if (opdsModel?.type == 10) {
                    OPDS1Parser.parseURL(URL(it))
                } else {
                    OPDS2Parser.parseURL(URL(it))
                }
            } catch (e: MalformedURLException) {
                //progress.dismiss()
                snackbar(act.coordinatorLayout(), "Failed parsing OPDS")
            }
            title = opdsModel?.title
        }

        parsePromise?.successUi { result ->
            facets = result.feed?.facets ?: mutableListOf()
            val publicationsList = result.feed?.publications!!

            if (facets.size > 0) {
                showFacetMenu = true
            }
            invalidateOptionsMenu()

            launch {
                nestedScrollView {
                    padding = dip(10)

                    linearLayout {
                        orientation = LinearLayout.VERTICAL

                        for (navigation in result.feed!!.navigation) {
                            button {
                                text = navigation.title
                                onClick {
                                    val model = OPDSModel(navigation.title!!, navigation.href.toString(), opdsModel?.type!!)
                                    //progress.show()
                                    startActivity(intentFor<OPDSCatalogActivity>("opdsModel" to model))
                                }
                            }
                        }

                        if (result.feed!!.publications.isNotEmpty()) {
                            mRecyclerView = recyclerView {
                                layoutManager = GridAutoFitLayoutManager(act, 120)
                                mLinearLayoutManager = layoutManager as LinearLayoutManager
                                for (i in 0 until 20) {
                                    currentPublicationsList.add(publicationsList[i])
                                }
                                adapter = RecyclerViewAdapter(act, currentPublicationsList)// For time being just passing 50 items to the adapter instead of passing 450 +, which makes the page loads very slow.
                            }
                            setRecyclerViewScrollListener(publicationsList)
                        }

                        for (group in result.feed!!.groups) {
                            if (group.publications.isNotEmpty()) {

                                linearLayout {
                                    orientation = LinearLayout.HORIZONTAL
                                    padding = dip(10)
                                    bottomPadding = dip(5)
                                    lparams(width = matchParent, height = wrapContent)
                                    weightSum = 2f
                                    textView {
                                        text = group.title
                                    }.lparams(width = wrapContent, height = wrapContent, weight = 1f)

                                    if (group.links.size > 0) {
                                        textView {
                                            text = context.getString(R.string.opds_list_more)
                                            gravity = Gravity.END
                                            onClick {
                                                val model = OPDSModel(group.title, group.links.first().href.toString(), opdsModel?.type!!)
                                                startActivity(intentFor<OPDSCatalogActivity>("opdsModel" to model))
                                            }
                                        }.lparams(width = wrapContent, height = wrapContent, weight = 1f)
                                    }
                                }

                                recyclerView {
                                    layoutManager = LinearLayoutManager(act)
                                    (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
                                    adapter = RecyclerViewAdapter(act, group.publications)
                                }
                            }
                            if (group.navigation.isNotEmpty()) {
                                for (navigation in group.navigation) {
                                    button {
                                        text = navigation.title
                                        onClick {
                                            val model = OPDSModel(navigation.title!!, navigation.href.toString(), opdsModel?.type!!)
                                            startActivity(intentFor<OPDSCatalogActivity>("opdsModel" to model))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //progress.dismiss()
            }
        }

        parsePromise?.fail {
            launch {
                //progress.dismiss()
//                snackbar(act.coordinatorLayout(), it.message!!)
            }
            if (DEBUG) Timber.e(it)
        }

    }

    private fun setRecyclerViewScrollListener(publicationsList: MutableList<org.readium.r2.shared.publication.Publication>) {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager?.itemCount
                if (totalItemCount != null) {
                    if (!recyclerView.canScrollVertically(1) && totalItemCount <= publicationsList.size) {
                        val updatedList = publicationsList.toList()
                        for (i in totalItemCount until totalItemCount + 20) {
                            if (i < updatedList.size)
                                currentPublicationsList.add(updatedList[i])
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }


        }
        mRecyclerView.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        //progress.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)

        return showFacetMenu
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.filter -> {
                facetPopup = facetPopUp()
                facetPopup?.showAsDropDown(this.findViewById(R.id.filter), 0, 0, Gravity.END)
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun facetPopUp(): PopupWindow {

        val layoutInflater = LayoutInflater.from(this)
        val layout = layoutInflater.inflate(R.layout.filter_window, null)
        val userSettingsPopup = PopupWindow(this)
        userSettingsPopup.contentView = layout
        userSettingsPopup.width = ListPopupWindow.WRAP_CONTENT
        userSettingsPopup.height = ListPopupWindow.WRAP_CONTENT
        userSettingsPopup.isOutsideTouchable = true
        userSettingsPopup.isFocusable = true

        val adapter = MergeAdapter()
        for (i in facets.indices) {
            adapter.addView(headerLabel(facets[i].title))
            for (link in facets[i].links) {
                adapter.addView(linkCell(link))
            }
        }

        val facetList = layout.findViewById<ListView>(R.id.facetList)
        facetList.adapter = adapter

        return userSettingsPopup
    }

    private fun headerLabel(value: String): View {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.section_header, null) as LinearLayout
        layout.header.text = value
        return layout
    }

    private fun linkCell(link: Link?): View {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.filter_row, null) as LinearLayout
        layout.text.text = link!!.title
        layout.count.text = link.properties.numberOfItems?.toString()
        layout.setOnClickListener {
            val model = OPDSModel(link.title!!, link.href.toString(), opdsModel?.type!!)
            facetPopup?.dismiss()
            startActivity(intentFor<OPDSCatalogActivity>("opdsModel" to model))
        }
        return layout
    }

    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView.webViewClient = WebViewClient()
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadTimeStart = System.currentTimeMillis()
                Timber.d("--->Webview loading Started-->>" + loadTimeStart)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadTimeEnd = System.currentTimeMillis() // - loadTimeStart
                Timber.d("--->Webview loading finished-->>"+((loadTimeEnd - loadTimeStart) / 1000))
            }
        }
    }

    private fun loadUrl() {
        // this will load the url of the website
        webView.loadUrl("https://readium.firebaseapp.com/?")
    }

    // if you press Back button this code will work
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // if your webview can go back it will go back
        if (keyCode == KeyEvent.KEYCODE_BACK && this.webView.canGoBack()) {
            this.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
