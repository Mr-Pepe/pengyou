package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import kotlinx.android.synthetic.main.fragment_stroke_order.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask


class StrokeOrderFragment : Fragment() {
    private lateinit var model: WordViewViewModel
    private lateinit var webView : WebView
    var character : String = "我"

    private var currentStrokeOrder = ""
    private var nStrokes = 0
    private var currentStroke = 0

    private var isAnimating = MutableLiveData<Boolean>()

    private var resetRequest = false
    private var resetFinished = MutableLiveData<Boolean>()
    private var startAnimatingAfterReset = false

    private var completedStroke = MutableLiveData<Boolean>()
    private var block = false

    private var showCharacterRequest = false
    private var showCharacterFinished = MutableLiveData<Boolean>()

    private var isQuizzing = MutableLiveData<Boolean>()

    private var diagramSize = 0

    private lateinit var strokeOrderDiagramList: RecyclerView
    private lateinit var adapter: StrokeOrderDiagramAdapter

    private lateinit var toggleHorizontalPagingListener: StrokeOrderFragment.ToggleHorizontalPagingListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ToggleHorizontalPagingListener) {
            toggleHorizontalPagingListener = parentFragment as ToggleHorizontalPagingListener
        }
        else {
            throw ClassCastException(
                "$parentFragment must implement ToggleHorizontalPaging."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragment?.let {
            model = ViewModelProvider(it).get(WordViewViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        model.entry.observe(this, Observer { entry ->
            character = entry.simplified
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_stroke_order, container, false)

        return root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        strokeOrderDiagramList = view.strokeOrderDiagramList

        adapter = StrokeOrderDiagramAdapter()
        strokeOrderDiagramList.layoutManager = LinearLayoutManager(activity)
        strokeOrderDiagramList.adapter = adapter

        buttonPlay.setOnClickListener {

//            if (!block) {
//                block = true
//                if (isAnimating.value!!) {
//                    isAnimating.value = false
//                }
//                else {
//                    isAnimating.value = true
//
//                    if (currentStroke == nStrokes) {
//                        startAnimatingAfterReset = true
//                        MainScope().launch {
//                            webView.runJavaScript("reset()")
//                        }
//                    }
//                    else {
//                        animateStroke()
//                    }
//                }
//            }
        }

        buttonNext.setOnClickListener {
//            if (!block) {
//                if (!isAnimating.value!!) {
//                    block = true
//                    if (currentStroke == nStrokes) {
//                        startAnimatingAfterReset = true
//                        MainScope().launch {
//                            webView.runJavaScript("reset()")
//                        }
//                    }
//                    else {
//                        animateStroke()
//                    }
//                }
//            }
        }

        buttonReset.setOnClickListener {
//            block = true
//            if (isAnimating.value!!) {
//                resetRequest = true
//                isAnimating.value = false
//            }
//            else {
//                MainScope().launch {
//                    webView.runJavaScript("reset()")
//                }
//            }
        }

//        buttonFull.setOnClickListener {
//            block = true
//            if (isAnimating.value!!) {
//                showCharacterRequest = true
//                isAnimating.value = false
//            }
//            else {
//                MainScope().launch {
//                    webView.runJavaScript("showCharacter()")
//                }
//            }
//        }

        buttonOutline.setOnClickListener {
//            when (outlineMode) {
//                OutlineMode.SHOW -> {
//                    outlineMode = OutlineMode.HIDE
//                    webView.runJavaScript("hideOutline()")
//                    buttonOutline.contentDescription = getString(R.string.button_outline_show)
//                }
//                OutlineMode.HIDE -> {
//                    outlineMode = OutlineMode.SHOW
//                    webView.runJavaScript("showOutline()")
//                    buttonOutline.contentDescription = getString(R.string.button_outline_hide)
//                }
//            }
        }

        buttonQuiz.setOnClickListener {
//            toggleHorizontalPagingListener.toggleHorizontalPaging()
//            if (!isQuizzing.value!!) {
//                isQuizzing.value = true
//                MainScope().launch {
//                    webView.runJavaScript("startQuiz()")
//                }
//            }
//            else {
//                isQuizzing.value = false
//                MainScope().launch {
//                    webView.runJavaScript("reset()")
//                }
//            }
        }


        model.strokeOrders.observe(viewLifecycleOwner, Observer {strokeOrders ->

            val cleanStrokeOrders = mutableListOf<String>()
//            currentStrokeOrder = cleanStrokeOrders[0].replace("\'", "\"")

            strokeOrders.forEach { strokeOrder ->
                cleanStrokeOrders.add(strokeOrder.replace("\'", "\""))
            }
            // TODO: Handle multiple characters and missing stroke order diagrams
            if (!cleanStrokeOrders.isEmpty()) {
//                Log.d("", currentStrokeOrder)

                adapter.setEntries(cleanStrokeOrders)

//                nStrokes =
//                    (Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject).array<String>(
//                        "strokes"
//                    )!!.size
//                currentStroke = 0
}
        })
    }

    fun resizeAndLoad(){



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
                    buttonPlay.setImageResource(R.drawable.ic_pause)
                    buttonPlay.contentDescription = getString(R.string.button_play_pause)
                    buttonNext.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                    buttonQuiz.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                }
                false -> {
                    buttonPlay.setImageResource(R.drawable.ic_play_arrow)
                    buttonPlay.contentDescription = getString(R.string.button_play_play)
                    buttonNext.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                    buttonQuiz.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                }
            }
        })

        isQuizzing.observe(viewLifecycleOwner, Observer {
            when(it) {
                true -> {
                    buttonPlay.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                    buttonNext.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
//                    buttonFull.isEnabled = false
                    webView.setOnTouchListener { _, _ -> false}

                }
                false -> {
                    buttonPlay.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                    buttonNext.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
//                    buttonFull.isEnabled = true
                    webView.setOnTouchListener(object : View.OnTouchListener {
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                            return (event?.action == MotionEvent.ACTION_MOVE)
                        }
                    })
                }
            }
        })


    }

    override fun onDestroy() {
//        if (::webView.isInitialized) {
//            webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
//        }
        super.onDestroy()
    }

    interface ToggleHorizontalPagingListener {
        fun toggleHorizontalPaging()
    }

//    private inner class JavaScriptInterface {
////        @JavascriptInterface
////        fun getJSON(): String {
////            val json : JsonObject = Parser.default().parse(StringBuilder(currentStrokeOrder)) as JsonObject
////
////            return json.toJsonString()
////        }
////
////        @JavascriptInterface
////        fun isLoaded() {
////            webViewLoaded = true
////            initCharacter()
////        }
//
//        @JavascriptInterface
//        fun strokeComplete() {
//            var timer1 = Timer()
//                timer1.schedule(timerTask { completedStroke.postValue(true) }, 10)
//
//        }
//
//        @JavascriptInterface
//        fun resetFinished() {
//            var timer1 = Timer()
//                timer1.schedule(timerTask { resetFinished.postValue(true) }, 200)
//        }
//
//        @JavascriptInterface
//        fun getCurrentStroke(): Int{
//            return currentStroke
//        }
//
//        @JavascriptInterface
//        fun showCharacterFinished() {
//            var timer1 = Timer()
//                timer1.schedule(timerTask { showCharacterFinished.postValue(true) }, 100)
//        }
//
//    }

    private fun animateStroke() {
        MainScope().launch {
            webView.runJavaScript("animateStroke()")
        }
    }

//    private fun initCharacter() {
//        if (webViewLoaded && currentStrokeOrder != "") {
//            MainScope().launch {
//                webView.runJavaScript("initCharacter()")
//            }
//        }
//    }

    enum class OutlineMode {
        SHOW,
        HIDE
    }
}


