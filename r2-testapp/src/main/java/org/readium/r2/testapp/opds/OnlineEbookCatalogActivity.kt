package org.readium.r2.testapp.opds

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_webview.*
import org.readium.r2.testapp.R
import timber.log.Timber


class OnlineEbookCatalogActivity : AppCompatActivity() {
    private var loadTimeStart: Long = 0
    private var loadTimeEnd: Long = 0
    private var progressDialog:ProgressDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_webview)
        initWebView()
        loadUrl()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Loading...")
        progressDialog!!.setMessage("Book is loading, please wait")
        progressDialog!!.show()
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
                progressDialog!!.dismiss()
                loadTimeEnd = System.currentTimeMillis() // - loadTimeStart
                Timber.d("--->Webview loading finished-->>"+((loadTimeEnd - loadTimeStart) / 1000))
            }
        }
    }

    private fun loadUrl() {
        // this will load the url of the website
        webView.loadUrl("https://readium.firebaseapp.com/?")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // if your webview can go back it will go back
        try {
            if(this.webView.canGoBack()!=null){
                if (keyCode == KeyEvent.KEYCODE_BACK && this.webView.canGoBack()) {
                    this.webView.goBack()
                    return true
                }
            }
        } catch (e: Exception) {
        }

        return super.onKeyDown(keyCode, event)
    }
}