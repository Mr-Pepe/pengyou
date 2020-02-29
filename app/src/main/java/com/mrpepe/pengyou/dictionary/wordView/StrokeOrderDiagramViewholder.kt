package com.mrpepe.pengyou.dictionary.wordView

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.stroke_order_diagram.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.min

class StrokeOrderDiagramViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var strokeOrder = ""
    var webViewLoaded = false
    lateinit var webView: WebView

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

    init {
        itemView.post(object : Runnable {
            override fun run() {
                viewTreeObserver = itemView.viewTreeObserver as ViewTreeObserver
                viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
            }
        })
    }

    fun bind(strokeOrder: String) {
        this.strokeOrder = strokeOrder
    }

    fun resizeAndLoad() {
        viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)


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
    }

    private fun initCharacter() {
        if (webViewLoaded && strokeOrder != "") {
            MainScope().launch {
                webView.runJavaScript("initCharacter()")
            }
        }
    }

}
