package com.mrpepe.pengyou.dictionary.searchView

import kotlinx.android.synthetic.main.fragment_handwriting_input.*

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import java.lang.Exception
import java.lang.StringBuilder


class HandwritingFragment : Fragment() {
    private lateinit var model: SearchViewFragmentViewModel
    private lateinit var webView : WebView

    private val BASE_URL = "file:///android_asset/handwriting_input/index.html"
    private val JAVASCRIPT_OBJ = "Android"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProviders.of(it).get(SearchViewFragmentViewModel::class.java)
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

//        webView = handwriting_web_view
//        webView.settings.javaScriptEnabled = true
//        webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView?, url: String?) {
//            }
//        }
//
//        webView.loadUrl(BASE_URL)
    }

    override fun onDestroy() {
//        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
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
    }
}
