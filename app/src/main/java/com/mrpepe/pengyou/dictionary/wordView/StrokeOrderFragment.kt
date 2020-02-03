package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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
import java.util.*
import kotlin.concurrent.timerTask


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

    private var isAnimating = MutableLiveData<Boolean>()

    private var outlineMode = OutlineMode.SHOW

    private var resetRequest = false
    private var resetFinished = MutableLiveData<Boolean>()
    private var startAnimatingAfterReset = false

    private var completedStroke = MutableLiveData<Boolean>()
    private var block = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProviders.of(it).get(WordViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.entry.observe(this, Observer { entry ->
            character = entry.simplified
        })

        isAnimating.value = false
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
            initCharacter()
        })

        buttonPlay.setOnClickListener {

            if (!block) {
                block = true
                if (isAnimating.value!!) {
                    isAnimating.value = false
                }
                else {
                    isAnimating.value = true

                    if (currentStroke == nStrokes) {
                        startAnimatingAfterReset = true
                        MainScope().launch {
                            webView.runJavaScript("reset()")
                        }
                    }
                    else {
                        animateStroke()
                    }
                }
            }
        }

        buttonNext.setOnClickListener {
            if (!block) {
                if (!isAnimating.value!!) {
                    block = true
                    if (currentStroke == nStrokes) {
                        startAnimatingAfterReset = true
                        MainScope().launch {
                            webView.runJavaScript("reset()")
                        }
                    }
                    else {
                        animateStroke()
                    }
                }
            }
        }

        buttonReset.setOnClickListener {
            block = true
            if (isAnimating.value!!) {
                resetRequest = true
                isAnimating.value = false
            }
            else {
                MainScope().launch {
                    webView.runJavaScript("reset()")
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

        resetFinished.observe(this, Observer {
            currentStroke = 0

            if (startAnimatingAfterReset) {
                startAnimatingAfterReset = false
                animateStroke()
            }
            else {
                block = false
            }
        })

        completedStroke.observe(this, Observer {
            if (resetRequest) {
                resetRequest = false
                MainScope().launch {
                    webView.runJavaScript("reset()")
                }
            } else {
                block = false
                currentStroke++

                if (currentStroke == nStrokes) {
                    isAnimating.value = false

                }
                else {
                    animateStroke()
                }
            }
        })

        isAnimating.observe(this, Observer {
            when(it) {
                true -> buttonPlay.text = getString(R.string.button_play_pause)
                false -> buttonPlay.text = getString(R.string.button_play_play)
            }
        })
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun getJSON(): String {
            val json : JsonObject = Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject

            return json.toJsonString()
        }

        @JavascriptInterface
        fun isLoaded() {
            webViewLoaded = true
            initCharacter()

        }

        @JavascriptInterface
        fun strokeComplete() {
            var timer1 = Timer()
                timer1.schedule(timerTask { completedStroke.postValue(true) }, 10)

        }

        @JavascriptInterface
        fun resetFinished() {
            var timer1 = Timer()
                timer1.schedule(timerTask { resetFinished.postValue(true) }, 500)
        }

        @JavascriptInterface
        fun getCurrentStroke(): Int{
            return currentStroke
        }
    }

    private fun animateStroke() {
        MainScope().launch {
            webView.runJavaScript("animateStroke()")
        }
    }

    private fun initCharacter() {
        if (webViewLoaded && currentStrokeOrder != "") {
            webView.runJavaScript("initCharacter()")
        }
    }

    enum class OutlineMode {
        SHOW,
        HIDE
    }
}


