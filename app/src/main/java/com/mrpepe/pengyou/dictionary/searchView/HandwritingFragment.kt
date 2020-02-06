package com.mrpepe.pengyou.dictionary.searchView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.webkit.WebViewAssetLoader
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.runJavaScript
import kotlinx.android.synthetic.main.fragment_handwriting_input.*
import java.io.Console
import java.util.*
import kotlin.concurrent.timerTask


class HandwritingFragment : Fragment() {
    private lateinit var model: SearchViewFragmentViewModel
    private lateinit var webView : WebView

    private val BASE_URL = "https://appassets.androidplatform.net/assets/handwriting_input/hanzi_lookup.html"
    private val JAVASCRIPT_OBJ = "Android"

    private var isLoaded = MutableLiveData<Boolean>()

    private var proposedCharactersString = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProvider(this).get(SearchViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_handwriting_input, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = hanzi_lookup_webview
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(MainApplication.getContext()))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(MainApplication.getContext()))
            .build()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request?.url!!)
            }
        }

        webView.loadUrl(BASE_URL)

        isLoaded.observe(viewLifecycleOwner, Observer {

//            webView.runJavaScript("init()")
//            Thread.sleep(1000)
            webView.runJavaScript("feedback()")
        })

        proposedCharactersString.observe(viewLifecycleOwner, Observer {
            proposedCharacters.text = it
        })
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

    private inner class JavaScriptInterface {
        //        @JavascriptInterface
//        fun getJSON(): String {
////            val json : JsonObject = Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject
//
////            return json.toJsonString()
//            return "1"
//        }
        @JavascriptInterface
        fun feedback(string: String) {
            Timer().schedule(timerTask { proposedCharactersString.postValue(string) }, 10)
        }

        @JavascriptInterface
        fun isLoaded() {
            Timer().schedule(timerTask { isLoaded.postValue(true) }, 10)
        }
    }
}
