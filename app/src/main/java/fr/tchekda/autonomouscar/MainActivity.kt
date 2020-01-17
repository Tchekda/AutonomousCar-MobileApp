package fr.tchekda.autonomouscar

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var handler: Handler
    private var socketThread: SocketThread? = null
    private var data: MutableMap<String, String> = HashMap()
    private var receivedData: MutableMap<String, String> = HashMap()

    init {
        val outerClass = WeakReference(this)
        handler = MyHandler(outerClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val dev = true
        initSocket(true)
        //switch_dev.isChecked = dev


        switch_dev.setOnCheckedChangeListener { _, isChecked ->
            socketThread?.disconnect()
            socketThread?.interrupt()
            initSocket(isChecked)
        }

        bar_speed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    val finalSpeed = progress + 116
                    data["speed"] = finalSpeed.toString()
                    data["keep"] = "1"
                    sendData()
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        button_arrow_up.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    val speed = receivedData["speed"]!!.toInt()
                    //toast("Down $speed")
                }
                MotionEvent.ACTION_UP -> {
                    //toast("UP")
                }
            }
            return@OnTouchListener true
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        socketThread?.disconnect()
    }

    fun initSocket(dev: Boolean){
        socketThread = SocketThread(dev)
        socketThread?.start()
        socketThread?.setHandler(handler)
    }

    fun sendData() {
        Thread {
            socketThread!!.sendMessage(data)
        }.start()
    }



    class MyHandler(private val outerClass: WeakReference<MainActivity>) : Handler() {

        override fun handleMessage(msg: Message?) {
            val receivePairs = msg?.obj.toString().split("|")
            val receiveData: MutableMap<String, String> = HashMap()
            receivePairs.forEach {
                val split = it.split("=")
                if (split.size == 2) {
                    receiveData[split[0]] = split[1]
                }
            }
            receiveData.forEach { (key, value) ->
                when (key) {
                    "speed" -> {
                        outerClass.get()?.text_speed_data?.text = value
                        outerClass.get()?.bar_speed?.progress = (value.toInt() - 116)
                    }
                    "connect" -> {
                        if (value.toInt() == 1) {
                            outerClass.get()?.text_connect_data?.text =
                                outerClass.get()?.getString(R.string.connection_on)
                        } else {
                            outerClass.get()?.text_connect_data?.text =
                                outerClass.get()?.getString(R.string.connection_off)
                        }
                    }
                }
            }
            outerClass.get()?.receivedData = receiveData
        }
    }
}
