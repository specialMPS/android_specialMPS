package com.example.specialmps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageListAdapter(val context: Context, val messageList: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_MESSAGE_SENT = 1
    val VIEW_TYPE_MESSAGE_RECEIVED = 2

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //var user_date:TextView
        var user_message: TextView
        var user_time: TextView

        init {
            // user_date = itemView.findViewById(R.id.text_chat_date_me)
            user_message = itemView.findViewById(R.id.text_chat_message_me)
            user_time = itemView.findViewById(R.id.text_chat_timestamp_me)
        }

        fun bind(message: Message) {
            // user_date.text=message.date
            user_message.text = message.message
            user_time.text = message.time
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // var chatbot_date:TextView
        var chatbot_message: TextView
        var chatbot_time: TextView

        init {
            //  chatbot_date = itemView.findViewById(R.id.text_chat_date_other)
            chatbot_message = itemView.findViewById(R.id.text_chat_message_other)
            chatbot_time = itemView.findViewById(R.id.text_chat_timestamp_other)
        }

        fun bind(message: Message) {
            //  chatbot_date.text = message.date
            chatbot_message.text = message.message
            chatbot_time.text = message.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            v = LayoutInflater.from(parent.context).inflate(R.layout.chat_ballon, parent, false)
            return SenderViewHolder(v)
        }
        //else if(viewType==VIEW_TYPE_MESSAGE_RECEIVED){
        else {
            v = LayoutInflater.from(parent.context).inflate(R.layout.chat_ballon_etc, parent, false)
            return ReceiverViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder.itemViewType == VIEW_TYPE_MESSAGE_SENT) {
            //if(messageList[position].sender=="User")    {
            (holder as SenderViewHolder).bind(messageList[position])

        } else if (holder.itemViewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            (holder as ReceiverViewHolder).bind(messageList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].sender == "user") {
            return VIEW_TYPE_MESSAGE_SENT
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

}
