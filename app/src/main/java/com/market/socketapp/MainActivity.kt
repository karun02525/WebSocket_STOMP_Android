package com.market.socketapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.JsonObject
import com.market.socketapp.Const.BaseURL
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class MainActivity : AppCompatActivity() {

    private var mStompClient: StompClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isStart()
    }



    private fun isStart(){
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BaseURL);
        mStompClient!!.connect()
    }



    fun btnJoinChat(v: View){
        val username = username.text.toString()
        if (username.isBlank()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show()
        } else {
            sendRegister(username)
        }
    }

    @SuppressLint("CheckResult")
    fun sendRegister(userName:String) {
        val innerObject = JsonObject()
        innerObject.addProperty("sender", userName)
        innerObject.addProperty("type", "JOIN")

        mStompClient!!.send("/app/chat.register", innerObject.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                startActivity(Intent(this, ChatActivity::class.java)
                    .putExtra("username", userName))
            }, { e -> toast(e.message!!) })

    }

    private fun toast(text: String) {
        Log.d("myTag", "messgae $text")
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        mStompClient!!.disconnect();
        super.onDestroy()
    }
}
