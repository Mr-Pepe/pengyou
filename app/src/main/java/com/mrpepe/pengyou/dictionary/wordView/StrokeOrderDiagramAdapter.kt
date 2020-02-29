package com.mrpepe.pengyou.dictionary.wordView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.search.SearchResultViewHolder

class StrokeOrderDiagramAdapter:
    RecyclerView.Adapter<StrokeOrderDiagramViewholder>() {

    var strokeOrders = emptyList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrokeOrderDiagramViewholder {
        return StrokeOrderDiagramViewholder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.stroke_order_diagram,
                    parent,
                    false
                ) as CardView
        )
    }

    override fun onBindViewHolder(holder: StrokeOrderDiagramViewholder, position: Int) {
        holder.bind(this.strokeOrders[position])
    }

    override fun getItemCount(): Int {
        return this.strokeOrders.size
    }

    internal fun setEntries(strokeOrders: List<String>) {
        this.strokeOrders = strokeOrders
        notifyDataSetChanged()
    }
}
