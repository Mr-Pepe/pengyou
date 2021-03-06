package com.mrpepe.pengyou.dictionary.wordView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.StrokeOrder

class StrokeOrderDiagramAdapter(private val viewLifecycleOwner: LifecycleOwner, val fragment: StrokeOrderFragment):
    RecyclerView.Adapter<StrokeOrderDiagramViewholder>() {

    private var strokeOrders = emptyList<StrokeOrder>()

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
        holder.bind(this.strokeOrders[position], viewLifecycleOwner, fragment, position)
    }

    override fun getItemCount(): Int {
        return this.strokeOrders.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    internal fun setEntries(strokeOrders: List<StrokeOrder>) {
        this.strokeOrders = strokeOrders
        notifyDataSetChanged()
    }
}
