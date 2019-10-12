package com.market.socketapp

import android.annotation.SuppressLint
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import com.market.socketapp.Const.BaseURL
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_message_list.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage


class ChatActivity : AppCompatActivity() {

    private var mStompClient: StompClient? = null
    private var username:String? = null
    var list:ArrayList<ChatMessage> = arrayListOf()
    var adapter:DataAdapter?=null


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        username=intent.getStringExtra("username")?:""
        tv_title.text=username
        adapter = DataAdapter(listOf(),username!!)
        recyclerViewID.adapter = adapter
        isStart()
        lifecycle()

        btnSend.setOnClickListener {
            sendMessage()
        }
    }



    @SuppressLint("CheckResult")
    fun lifecycle(){
        mStompClient!!.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                   // LifecycleEvent.Type.OPENED -> toast("Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> toast("Stomp connection error")
                    LifecycleEvent.Type.CLOSED -> toast("Stomp connection closed")
                }
            }
    }


    @SuppressLint("CheckResult")
    fun isStart(){
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BaseURL);
        mStompClient!!.connect()

        mStompClient!!.topic("/topic/public")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { topicMessage ->
                parseData(topicMessage)
            }

    }

    private fun parseData(topicMessage: StompMessage?) {
        val payload=topicMessage!!.payload
        val json=JSONObject(payload)
        val senderName=json.getString("sender")
        val type=json.getString("type")
        val content=json.getString("content")
        if(type !="JOIN") {
            list.add(ChatMessage(senderName, type, content))
            adapterNotify()
        }
    }


    @SuppressLint("CheckResult")
    private fun sendMessage() {
        val mess = enter_message.text.toString()
        if (mess.isBlank()) {
            toast("Please any message")
        } else {
            val innerObject = JsonObject()
            innerObject.addProperty("sender", username)
            innerObject.addProperty("type", "CHAT")
            innerObject.addProperty("content", mess)

            mStompClient!!.send("/app/chat.send", innerObject.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    enter_message.text.clear()
                }, { e -> toast(e.message!!) })
        }

    }

    private fun adapterNotify(){

        adapter!!.items = list
        adapter!!.notifyDataSetChanged()
        //ringtone()
        scrollToBottom()
    }

    private fun toast(text: String) {
        Log.d("myTag", "messgae $text")
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        mStompClient!!.disconnect();
        super.onStop()
    }

    private fun scrollToBottom() {
        recyclerViewID.scrollToPosition(adapter!!.itemCount - 1)
    }

    private fun ringtone() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.URI_COLUMN_INDEX)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun btnBack(v: View) {
       onBackPressed()
    }
}
