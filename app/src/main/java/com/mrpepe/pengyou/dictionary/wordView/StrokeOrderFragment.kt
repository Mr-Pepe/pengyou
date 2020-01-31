package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.runJavaScript
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.StringBuilder


class StrokeOrderFragment : Fragment() {
    private lateinit var model: WordViewFragmentViewModel
    private lateinit var webView : WebView
    var character : String = "我"
    var webViewLoaded : Boolean = false

    private val BASE_URL = "file:///android_asset/stroke_order/stroke_order.html"
    private val JAVASCRIPT_OBJ = "Android"

    private var currentStrokeOrder = ""
    private var nStrokes = 0
    private var currentStroke = 0

    private var isAnimating = false

    private var outlineMode = OutlineMode.SHOW

    private var buttonsblocked = false

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
        return inflater.inflate(R.layout.fragment_stroke_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = my_web_view
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
        webView.loadUrl(BASE_URL)
        webView.settings.loadWithOverviewMode = true
//        webView.settings.useWideViewPort = true


        model.strokeOrders.observe(this, Observer {strokeOrders ->
            currentStrokeOrder = strokeOrders[0].replace("\'", "\"")
            nStrokes = (Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject).array<String>("strokes")!!.size
            currentStroke = 0
            showCharacter()
        })

        buttonPlay.setOnClickListener {

            if (!buttonsblocked) {
                if (!isAnimating) {
                    buttonsblocked = true
                    if (currentStroke == nStrokes) {
                        currentStroke = 0
                        webView.runJavaScript("reset()")
                        Thread.sleep(100)
                    }
                    isAnimating = true
                    buttonPlay.text = getString(R.string.button_play_pause)
                    animateStroke()
                } else {
                    buttonsblocked = true
                    isAnimating = false
                    buttonPlay.text = getString(R.string.button_play_play)
                    Thread.sleep(100)
                    buttonsblocked = false
                }
            }
        }

        buttonReset.setOnClickListener {
            buttonsblocked = true
            isAnimating = false
            buttonPlay.text = getString(R.string.button_play_play)
            webView.runJavaScript("reset()")
            Thread.sleep(100)
            currentStroke = 0
            buttonsblocked = false

        }

        buttonNext.setOnClickListener {
            if (!buttonsblocked and !isAnimating) {
                if (currentStroke in 0 until nStrokes) {
                    buttonsblocked = true
                    animateStroke()
                }
            }
        }

        buttonOutline.setOnClickListener {
            when (outlineMode) {
                OutlineMode.SHOW -> {
                    outlineMode = OutlineMode.HIDE
                    webView.runJavaScript("hideOutline()")
                    buttonOutline.text = getString(R.string.button_outline_show)
                }
                OutlineMode.HIDE -> {
                    outlineMode = OutlineMode.SHOW
                    webView.runJavaScript("showOutline()")
                    buttonOutline.text = getString(R.string.button_outline_hide)
                }
            }
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

        @JavascriptInterface
        fun isLoaded() {
            webViewLoaded = true

            if (currentStrokeOrder != "")
                MainScope().launch {
                    showCharacter()
                }
        }

        @JavascriptInterface
        fun strokeComplete() {
            currentStroke++

            buttonsblocked = false

            if (isAnimating && currentStroke in 0 until nStrokes) {
                MainScope().launch {
                    animateStroke()
                }
            }
            else if (currentStroke == nStrokes) {
                isAnimating = false
                buttonPlay.text = getString(R.string.button_play_play)
            }
        }

        @JavascriptInterface
        fun getCurrentStroke(): Int{
            return currentStroke
        }


    }

    private fun animateStroke() {
        webView.runJavaScript("animateStroke()")
    }

    private fun showCharacter() {
        if (webViewLoaded && currentStrokeOrder != "") {
            webView.runJavaScript("showCharacter()")
        }
    }



    enum class OutlineMode {
        SHOW,
        HIDE
    }
}


