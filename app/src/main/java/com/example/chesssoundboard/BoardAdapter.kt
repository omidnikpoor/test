package com.example.chesssoundboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BoardAdapter(
    private val cellCount: Int,
    private val hasSound: (Int) -> Boolean,
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<BoardAdapter.CellViewHolder>() {

    class CellViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val indicator: View = view.findViewById(R.id.soundIndicator)
        val label: TextView = view.findViewById(R.id.cellLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val row = position / 8
        val col = position % 8
        val isLight = (row + col) % 2 == 0

        holder.root.setBackgroundColor(
            holder.root.context.getColor(if (isLight) R.color.cell_light else R.color.cell_dark)
        )
        holder.label.text = (position + 1).toString()
        holder.indicator.visibility = if (hasSound(position)) View.VISIBLE else View.GONE

        holder.root.setOnClickListener { onClick(position) }
        holder.root.setOnLongClickListener {
            onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int = cellCount
}
