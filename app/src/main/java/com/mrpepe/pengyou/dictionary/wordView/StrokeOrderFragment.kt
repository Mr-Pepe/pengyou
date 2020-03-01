package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.StrokeOrder
import kotlinx.android.synthetic.main.fragment_stroke_order.*
import kotlinx.android.synthetic.main.fragment_stroke_order.view.*
import me.relex.circleindicator.CircleIndicator2


class StrokeOrderFragment : Fragment() {
    private lateinit var model: WordViewViewModel

    private lateinit var strokeOrderDiagramList: RecyclerView
    private lateinit var adapter: StrokeOrderDiagramAdapter
    private lateinit var layoutManager: StrokeOrderFragmentListLayoutManager
    private lateinit var strokeOrderPageIndicatorView: CircleIndicator2

    private var indicatorCount = 0
    private var currenPosition = 0

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stroke_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        strokeOrderDiagramList = view.strokeOrderDiagramList
        strokeOrderPageIndicatorView = view.strokeOrderPageIndicatorView

        adapter = StrokeOrderDiagramAdapter(viewLifecycleOwner, this)
        layoutManager = StrokeOrderFragmentListLayoutManager(activity?.baseContext!!)
        strokeOrderDiagramList.layoutManager = layoutManager
        strokeOrderDiagramList.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(strokeOrderDiagramList)
        strokeOrderPageIndicatorView.attachToRecyclerView(strokeOrderDiagramList, snapHelper)

        strokeOrderDiagramList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateButtonStates()
            }
        })

        buttonPlay.setOnClickListener {
            getCurrentDiagram()?.playButtonPressed()
        }

        buttonNext.setOnClickListener {
            getCurrentDiagram()?.nextButtonPressed()
        }

        buttonReset.setOnClickListener {
            getCurrentDiagram()?.resetButtonPressed()
        }

        buttonOutline.setOnClickListener {
            getCurrentDiagram()?.outlineButtonPressed()
        }

        buttonQuiz.setOnClickListener {
            getCurrentDiagram()?.quizButtonPressed()
        }


        model.strokeOrders.observe(viewLifecycleOwner, Observer {strokeOrders ->

            val filteredStrokeOrders = mutableListOf<StrokeOrder>()

            strokeOrders.forEachIndexed { iCharacter, strokeOrder ->
                if (model.entry.value!!.simplified[iCharacter].toString() !in listOf("，")){
                    filteredStrokeOrders.add(strokeOrder)
                    indicatorCount++
                }
            }

            strokeOrderPageIndicatorView.createIndicators(indicatorCount, 0)

            if (!filteredStrokeOrders.isEmpty()) {
                adapter.setEntries(filteredStrokeOrders)
            }
        })
    }

    private fun getCurrentDiagram(): StrokeOrderDiagramViewholder? {
        val position = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (position != RecyclerView.NO_POSITION) {
            currenPosition = position

            strokeOrderPageIndicatorView.animatePageSelected(currenPosition)
        }
        return when (strokeOrderDiagramList.findViewHolderForAdapterPosition(currenPosition) != null) {
            true -> strokeOrderDiagramList.findViewHolderForAdapterPosition(currenPosition) as StrokeOrderDiagramViewholder
            false -> null
        }
    }

    fun updateButtonStatesFromDiagram(position: Int) {
        if (position == layoutManager.findFirstCompletelyVisibleItemPosition()) {
            updateButtonStates()
        }
    }

    private fun updateButtonStates() {
        when (getCurrentDiagram()?.buttonPlayIsPlay ) {
            false -> {
                buttonPlay.setImageResource(R.drawable.ic_pause)
                buttonPlay.contentDescription = getString(R.string.button_play_pause)
            }
            true -> {
                buttonPlay.setImageResource(R.drawable.ic_play_arrow)
                buttonPlay.contentDescription = getString(R.string.button_play_play)
            }
        }

        when (getCurrentDiagram()?.buttonPlayEnabled) {
            false -> {
                buttonPlay.isEnabled = false
                buttonPlay.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
            }
            true -> {
                buttonPlay.isEnabled = true
                buttonPlay.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
            }
        }

        when (getCurrentDiagram()?.buttonNextEnabled) {
            false -> {
                buttonNext.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                buttonNext.isEnabled = false
            }
            true -> {
                buttonNext.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                buttonNext.isEnabled = true
            }
        }

        when (getCurrentDiagram()?.buttonQuizEnabled) {
            false -> {
                buttonQuiz.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                buttonQuiz.isEnabled = false
            }
            true -> {
                buttonQuiz.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                buttonQuiz.isEnabled = true
            }
        }

        when (getCurrentDiagram()?.buttonOutlineIsOutline) {
            false -> {
                buttonOutline.contentDescription = getString(R.string.button_outline_show)
            }
            true -> {
                buttonOutline.contentDescription = getString(R.string.button_outline_hide)
            }
        }

        when (getCurrentDiagram()?.buttonOutlineEnabled) {
            false -> {
                buttonOutline.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                buttonOutline.isEnabled = false
            }
            true -> {
                buttonOutline.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                buttonOutline.isEnabled = true
            }
        }

        when (getCurrentDiagram()?.buttonResetEnabled) {
            false -> {
                buttonReset.setColorFilter(getControlDisabledColor(), PorterDuff.Mode.SRC_IN)
                buttonReset.isEnabled = false
            }
            true -> {
                buttonReset.setColorFilter(getControlEnabledColor(), PorterDuff.Mode.SRC_IN)
                buttonReset.isEnabled = true
            }
        }
    }

    interface ToggleHorizontalPagingListener {
        fun toggleHorizontalPaging()
    }

    fun togglePaging() {
        toggleHorizontalPagingListener.toggleHorizontalPaging()
        layoutManager.scrollEnabled = !layoutManager.scrollEnabled
    }

    class StrokeOrderFragmentListLayoutManager(context: Context): LinearLayoutManager(context) {
        var scrollEnabled = true

        override fun canScrollVertically(): Boolean {
            return scrollEnabled && super.canScrollVertically()
        }

        override fun getExtraLayoutSpace(state: RecyclerView.State?): Int {
            return 500
        }
    }
}


