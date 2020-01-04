package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.extractDefinitions
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import kotlinx.android.synthetic.main.fragment_stroke_order.view.*
import kotlinx.android.synthetic.main.search_result.view.*
import java.lang.Exception


class StrokeOrderFragment : Fragment() {
    private lateinit var model: WordViewFragmentViewModel
    private lateinit var webView : WebView
    var character : String = "我"

    private val BASE_URL = "file:///android_asset/example.html"
    private val JAVASCRIPT_OBJ = "javascript_obj"

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
                    injectJavaScriptFunction()
                }
            }
        }

        webView.loadUrl(BASE_URL)

        btn_send_to_web.setOnClickListener {
            webView.evaluateJavascript(
                "javascript: " +
                        "updateFromAndroid(\"" + edit_text_to_web.text + "\")", null
            )
        }
    }



    override fun onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

    private fun injectJavaScriptFunction() {
        my_web_view.loadUrl("javascript: " +
                "window.androidObj.textToAndroid = function(message) { " +
                JAVASCRIPT_OBJ + ".textFromWeb(message) }")
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun textFromWeb(fromWeb: String) {
            txt_from_web.text = fromWeb
        }
    }
}
