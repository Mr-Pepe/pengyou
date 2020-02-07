package com.mrpepe.pengyou.dictionary.searchView

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.webkit.WebViewAssetLoader
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import com.mrpepe.pengyou.runJavaScript
import kotlinx.android.synthetic.main.fragment_handwriting_input.*
import kotlinx.android.synthetic.main.fragment_handwriting_input.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask


class HandwritingFragment : Fragment() {
    private lateinit var model: SearchViewFragmentViewModel
    private lateinit var webView : WebView
    private lateinit var proposedCharacterList: RecyclerView
    lateinit var adapter : ProposedCharacterAdapter

    private val BASE_URL = "https://appassets.androidplatform.net/assets/handwriting_input/hanzi_lookup.html"
    private val JAVASCRIPT_OBJ = "Android"

    private var isLoaded = MutableLiveData<Boolean>()

    private var proposedCharactersString = MutableLiveData<String>()

    private var strokes = listOf<List<List<Float>>?>()

    private val widthAndHeight = 250

    private val standardScale = 0.8.toFloat()

    private var minX = 10000000.toFloat()
    private var maxX = 0.toFloat()
    private var minY = 10000000.toFloat()
    private var maxY = 0.toFloat()
    private var scaleX = standardScale
    private var scaleY = standardScale

    private var lastClickTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProvider(it).get(SearchViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_handwriting_input, container, false)

        proposedCharacterList = root.proposedCharacters

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

        adapter = ProposedCharacterAdapter { character: String ->
            characterClicked(character)
        }

        proposedCharacterList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        proposedCharacterList.adapter = adapter


        isLoaded.observe(viewLifecycleOwner, Observer {
        })

        proposedCharactersString.observe(viewLifecycleOwner, Observer {
            proposedCharactersDummy.text = it
            var characters = mutableListOf<String>()

            val json : JsonArray<JsonObject> = Parser.default().parse(StringBuilder(it)) as JsonArray<JsonObject>

            json.forEach {character ->
                characters.add(character.get("hanzi").toString())
            }

            adapter.setProposedCharacters(characters)
        })

        draw_board.pathExposed.observe(viewLifecycleOwner, Observer {
            strokes = it

            for (iStroke in 0 until it.size) {

                it[iStroke].forEach {point ->

                    val x = point[0]
                    val y = point[1]

                    if (x > maxX)
                        maxX = x
                    if (x < minX)
                        minX = x
                    if (y > maxY)
                        maxY = y
                    if (y < minY)
                        minY = y

                }

            }

            if (maxX - minX > maxY - minY) {
                scaleX = standardScale
                scaleY = (maxY - minY) / (maxX - minX) * standardScale
            }
            else {
                scaleY = standardScale
                scaleX = (maxX - minX) / (maxY - minY) * standardScale
            }

            MainScope().launch {
                webView.runJavaScript("search()")
            }
        })

        clear_board_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                if (draw_board.isClear)
                    model.deleteLastCharacterOfQuery.postValue(true)
                draw_board.clearCanvas()
            }
        })

        search_button.setOnClickListener ( object: View.OnClickListener {
            override fun onClick(v: View?) {
                model.submitFromDrawBoard.value = true
            }
        } )
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }

    private inner class JavaScriptInterface {

        @JavascriptInterface
        fun updateProposedCharacters(string: String) {
            Timer().schedule(timerTask { proposedCharactersString.postValue(string) }, 10)
        }

        @JavascriptInterface
        fun isLoaded() {
            Timer().schedule(timerTask { isLoaded.postValue(true) }, 10)
        }

        @JavascriptInterface
        fun getNumberOfStrokes(): Int {
            return strokes.size
        }

        @JavascriptInterface
        fun getNumberOfPoints(iStroke: Int): Int {
            return strokes[iStroke]!!.size
        }

        // The x and y coordinates are scaled and centered so that they are in a 250x250 field with
        @JavascriptInterface
        fun getX(iStroke: Int, iPoint: Int): Float {
            return ((strokes[iStroke]!![iPoint][0] - minX ) / (maxX - minX) * widthAndHeight - widthAndHeight/2) * scaleX + widthAndHeight/2
        }

        @JavascriptInterface
        fun getY(iStroke: Int, iPoint: Int): Float {
            return ((strokes[iStroke]!![iPoint][1] - minY ) / (maxY - minY) * widthAndHeight - widthAndHeight/2) * scaleY + widthAndHeight/2
        }
    }

    private fun characterClicked(character: String){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        model.addCharacterToQuery.value = character

        draw_board.clearCanvas()
    }
}
