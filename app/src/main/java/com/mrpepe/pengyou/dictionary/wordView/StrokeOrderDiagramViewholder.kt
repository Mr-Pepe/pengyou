package com.mrpepe.pengyou.dictionary.wordView

import android.graphics.Color
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.stroke_order_diagram.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.min

class StrokeOrderDiagramViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var strokeOrder = ""
    var webViewLoaded = false
    lateinit var webView: WebView

    private var nStrokes = 0
    private var currentStroke = 0

    private var outlineMode = OutlineMode.SHOW

    private var isAnimating = MutableLiveData<Boolean>()

    private var resetRequest = false
    private var resetFinished = MutableLiveData<Boolean>()
    private var startAnimatingAfterReset = false

    private var completedStroke = MutableLiveData<Boolean>()
    private var block = false

    private var showCharacterRequest = false
    private var showCharacterFinished = MutableLiveData<Boolean>()

    private var isQuizzing = MutableLiveData<Boolean>()

    private lateinit var viewLifecycleOwner: LifecycleOwner
    private lateinit var fragment: StrokeOrderFragment
    private var index = -1

    private lateinit var viewTreeObserver: ViewTreeObserver
    private val onPreDrawListener = object:  ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            resizeAndLoad()
            return true
        }
    }

    private val BASE_URL = "file:///android_asset/stroke_order/stroke_order.html"
    private val JAVASCRIPT_OBJ = "Android"

    private var diagramSize = 0

    var buttonPlayIsPlay = true
    var buttonPlayEnabled = true
    var buttonNextEnabled = true
    var buttonQuizEnabled = true
    var buttonOutlineIsOutline = true

    init {
        itemView.post(object : Runnable {
            override fun run() {
                viewTreeObserver = itemView.viewTreeObserver as ViewTreeObserver
                viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
            }
        })

        isAnimating.value = false
        isQuizzing.value = false
    }

    fun bind(strokeOrder: String, viewLifecycleOwner: LifecycleOwner, fragment: StrokeOrderFragment, index: Int) {

        // TODO: Handle multiple characters and missing stroke order diagrams
        this.strokeOrder = strokeOrder
        this.viewLifecycleOwner = viewLifecycleOwner
        this.fragment = fragment
        this.index = index

        nStrokes = (Parser.default().parse(StringBuilder(strokeOrder)) as JsonObject).array<String>(
                        "strokes"
                    )!!.size
        currentStroke = 0
    }

    fun resizeAndLoad() {
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
        }

        val strokeOrderDiagramConstraintLayout = itemView.strokeOrderDiagramConstraintLayout
        val strokeOrderDiagramContainer = itemView.strokeOrderDiagramContainer
        webView = itemView.strokeOrderWebView

        diagramSize = (min(
                strokeOrderDiagramConstraintLayout.measuredHeight,
                strokeOrderDiagramConstraintLayout.measuredWidth
            ) * 0.9).toInt()

        val params = strokeOrderDiagramContainer.layoutParams
        params.height = diagramSize
        params.width = diagramSize
        strokeOrderDiagramContainer.requestLayout()

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JavaScriptInterface(), JAVASCRIPT_OBJ)
        webView.loadUrl(BASE_URL)
        webView.settings.loadWithOverviewMode = true
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.settings.useWideViewPort = true

        resetFinished.observe(viewLifecycleOwner, Observer {
            currentStroke = 0

            if (startAnimatingAfterReset) {
                startAnimatingAfterReset = false
                animateStroke()
            }
            else if (isQuizzing.value!!) {
                MainScope().launch {
                    webView.runJavaScript("startQuiz()")
                }
            }
            else {
                block = false
            }
        })

        showCharacterFinished.observe(viewLifecycleOwner, Observer {
            currentStroke = nStrokes
            block = false
        })

        completedStroke.observe(viewLifecycleOwner, Observer {
            if (resetRequest) {
                resetRequest = false
                MainScope().launch {
                    webView.runJavaScript("reset()")
                }
            } else if (showCharacterRequest) {
                showCharacterRequest = false
                MainScope().launch {
                    webView.runJavaScript("showCharacter()")
                }

            } else {
                block = false
                currentStroke++

                if (currentStroke == nStrokes) {
                    isAnimating.value = false
                }
                if (isAnimating.value!!) {
                    animateStroke()
                }
            }
        })

        isAnimating.observe(viewLifecycleOwner, Observer {
            when(it) {
                true -> {
                    buttonPlayIsPlay = false
                    buttonNextEnabled = false
                    buttonQuizEnabled = false
                }
                false -> {
                    buttonPlayIsPlay = true
                    buttonNextEnabled = true
                    buttonQuizEnabled = true
                }
            }
            fragment.updateButtonStatesFromDiagram(index)
        })

        isQuizzing.observe(viewLifecycleOwner, Observer {
            when(it) {
                true -> {
                    buttonPlayEnabled = false
                    buttonNextEnabled = false
                    webView.setOnTouchListener { _, _ -> false}
                }
                false -> {
                    buttonPlayEnabled = true
                    buttonNextEnabled = true
                    webView.setOnTouchListener(object : View.OnTouchListener {
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                            return (event?.action == MotionEvent.ACTION_MOVE)
                        }
                    })
                }
            }
            fragment.updateButtonStatesFromDiagram(index)
        })
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun getJSON(): String {
            val json: JsonObject = Parser.default().parse(StringBuilder(strokeOrder)) as JsonObject

            return json.toJsonString()
        }

        @JavascriptInterface
        fun isLoaded() {
            webViewLoaded = true
            initCharacter()
        }

        @JavascriptInterface
        fun getStrokeColor(): String {
            val typedValue = TypedValue()
            MainApplication.homeActivity.theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true)

            return String.format("#%06X", (0xFFFFFF and typedValue.data))
        }

        @JavascriptInterface
        fun getOutlineColor(): String {
            val typedValue = TypedValue()
            MainApplication.homeActivity.theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true)

            return when(MainApplication.homeActivity.isNightMode()) {
                true -> String.format("#%06X", 0xFFFFFF and Color.GRAY)
                false -> String.format("#%06X", 0xFFFFFF and Color.LTGRAY)
            }
        }

        @JavascriptInterface
        fun getSize(): Int {
            return (diagramSize)
//                    /MainApplication.getContext().resources.displayMetrics.density).toInt()
        }
        @JavascriptInterface
        fun strokeComplete() {
            Timer().schedule(timerTask { completedStroke.postValue(true) }, 10)

        }

        @JavascriptInterface
        fun resetFinished() {
            Timer().schedule(timerTask { resetFinished.postValue(true) }, 200)
        }

        @JavascriptInterface
        fun getCurrentStroke(): Int{
            return currentStroke
        }

        @JavascriptInterface
        fun showCharacterFinished() {
            Timer().schedule(timerTask { showCharacterFinished.postValue(true) }, 100)
        }
    }

    private fun initCharacter() {
        if (webViewLoaded && strokeOrder != "") {
            MainScope().launch {
                webView.runJavaScript("initCharacter()")
            }
        }
    }

    private fun animateStroke() {
        MainScope().launch {
            webView.runJavaScript("animateStroke()")
        }
    }

    fun playButtonPressed() {
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

    fun nextButtonPressed() {

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

    fun resetButtonPressed() {

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

    fun outlineButtonPressed() {

        when (outlineMode) {
            OutlineMode.SHOW -> {
                outlineMode = OutlineMode.HIDE
                webView.runJavaScript("hideOutline()")
                buttonOutlineIsOutline = false
            }
            OutlineMode.HIDE -> {
                outlineMode = OutlineMode.SHOW
                webView.runJavaScript("showOutline()")
                buttonOutlineIsOutline = true
            }
        }
        fragment.updateButtonStatesFromDiagram(index)
    }

    fun quizButtonPressed() {

        fragment.togglePaging()
        if (!isQuizzing.value!!) {
            isQuizzing.value = true
            MainScope().launch {
                webView.runJavaScript("startQuiz()")
            }
        }
        else {
            isQuizzing.value = false
            MainScope().launch {
                webView.runJavaScript("reset()")
            }
        }
    }

    enum class OutlineMode {
        SHOW,
        HIDE
    }
}
