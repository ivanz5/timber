package com.ivanzhur.timbertest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivanzhur.timbertest.R
import com.ivanzhur.timbertest.data.model.RecordModel
import com.ivanzhur.timbertest.databinding.ItemRecordBinding

class RecordListAdapter(
    private val onItemClicked: (RecordModel) -> Unit,
) : ListAdapter<RecordModel, RecordListAdapter.RecordViewHolder>(
    object : DiffUtil.ItemCallback<RecordModel>() {
        override fun areItemsTheSame(oldItem: RecordModel, newItem: RecordModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecordModel, newItem: RecordModel): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ui = ItemRecordBinding.bind(itemView)

        fun bind(item: RecordModel) {
            ui.text.text = item.id.toString()
            itemView.setOnClickListener { onItemClicked(item) }
        }
    }
}