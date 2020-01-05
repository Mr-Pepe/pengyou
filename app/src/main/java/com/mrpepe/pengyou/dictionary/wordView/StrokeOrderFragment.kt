package com.mrpepe.pengyou.dictionary.wordView

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
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import java.lang.Exception
import java.lang.StringBuilder


class StrokeOrderFragment : Fragment() {
    private lateinit var model: WordViewFragmentViewModel
    private lateinit var webView : WebView
    var character : String = "我"

    private val BASE_URL = "file:///android_asset/example.html"
    private val JAVASCRIPT_OBJ = "Android"

    private lateinit var currentStrokeOrder : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProviders.of(it).get(WordViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.entry.observe(this, Observer { entry ->
            character = entry.simplified
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_stroke_order, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = my_web_view
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url == BASE_URL) {
//                    injectJavaScriptFunction()
                }
            }
        }

        webView.loadUrl(BASE_URL)

        model.strokeOrders.observe(this, Observer {strokeOrders ->
            currentStrokeOrder = strokeOrders[0].replace("\'", "\"")

        })

        animateStrokeButton.setOnClickListener {
            webView.evaluateJavascript(
                "javascript: " +
                        "drawCharacter()", null
            )
        }
    }



    override fun onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

//    private fun injectJavaScriptFunction() {
//        my_web_view.loadUrl("javascript: " +
//                "window.androidObj.textToAndroid = function(message) { " +
//                JAVASCRIPT_OBJ + ".textFromWeb(message) }")
//    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun getJSON(): String {
            val json : JsonObject = Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject

            return json.toJsonString()
        }
    }
}
