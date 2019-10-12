package com.market.socketapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.adapter_item_received.view.*
import kotlinx.android.synthetic.main.adapter_item_sent.view.*


class DataAdapter(var items: List<ChatMessage>,val username: String) : RecyclerView.Adapter<DataAdapter.BaseViewHolder<*>>() {

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_sent, parent, false)
                SentViewHolder(view)
            }
            TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_received, parent, false)
                ReceivedViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = items[position]
        when (holder) {
            is SentViewHolder -> holder.bind(element)
            is ReceivedViewHolder -> holder.bind(element)
            else -> throw IllegalArgumentException()
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (items[position].sender) {
            in username -> TYPE_SENT
            !in username -> TYPE_RECEIVED
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    inner class SentViewHolder(itemView: View) : BaseViewHolder<ChatMessage>(itemView) {
        override fun bind(item: ChatMessage) = with(itemView) {
            if (item.content != "null") {
                message_text.visibility = View.VISIBLE
                message_text.text = item.content
            }
        }
    }

    inner class ReceivedViewHolder(itemView: View) : BaseViewHolder<ChatMessage>(itemView) {
        override fun bind(item: ChatMessage)  = with(itemView) {
            if (item.content != "null") {
                tvRev.visibility = View.VISIBLE
                tvRev.text = item.content
            }
        }
    }

}