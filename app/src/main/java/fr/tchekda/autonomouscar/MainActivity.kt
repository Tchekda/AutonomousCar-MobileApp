package fr.tchekda.autonomouscar

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
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

        initSocket(true)
        switch_dev.isChecked = true


        switch_dev.setOnCheckedChangeListener { _, isChecked ->
            socketThread?.disconnect()
            socketThread?.interrupt()
            initSocket(isChecked)
        }

        bar_speed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val finalSpeed = progress - 33
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

        button_stop.setOnClickListener {
            data["speed"] = "0"
            data["keep"] = "0"
            sendData()
        }

        val handler = Handler()

        val increaseSpeed: Runnable = object : Runnable {
            override fun run() {
                if (receivedData["speed"]!!.toInt() < 92) {
                    data["speed"] = receivedData["speed"]?.toInt()?.plus(2).toString()
                    val keep = if (switch_keep.isChecked) "1" else "0"
                    data["keep"] = keep
                    sendData()
                    handler.postDelayed(this, 100)
                }else if (receivedData["speed"]!!.toInt() == 92){
                    data["speed"] = receivedData["speed"]!!
                    val keep = if (switch_keep.isChecked) "1" else "0"
                    data["keep"] = keep
                    sendData()
                    handler.postDelayed(this, 100)
                }
            }
        }

        val decreaseSpeed: Runnable = object : Runnable {
            override fun run() {
                if (receivedData["speed"]!!.toInt() > -33) {
                    data["speed"] = receivedData["speed"]?.toInt()?.minus(2).toString()
                    val keep = if (switch_keep.isChecked) "1" else "0"
                    data["keep"] = keep
                    sendData()
                    handler.postDelayed(this, 100)
                }else if (receivedData["speed"]!!.toInt() == -33){
                    data["speed"] = receivedData["speed"]!!
                    val keep = if (switch_keep.isChecked) "1" else "0"
                    data["keep"] = keep
                    sendData()
                    handler.postDelayed(this, 100)
                }
            }
        }

        button_arrow_up.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            if (socketThread!!.isConnected()) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.postDelayed(increaseSpeed, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(increaseSpeed)
                    }
                }
            } else {
                toast("Not connected...")
            }
            return@OnTouchListener true
        })

        button_arrow_down.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            if (socketThread!!.isConnected()) {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.postDelayed(decreaseSpeed, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(decreaseSpeed)
                    }
                }
            } else {
                toast("Not connected...")
            }
            return@OnTouchListener true
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        socketThread?.disconnect()
    }

    fun initSocket(dev: Boolean) {
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
                        val speed = value.toInt() + 33
                        outerClass.get()?.text_speed_data?.text = value
                        outerClass.get()?.bar_speed?.progress = speed
                    }
                    "keep" -> {
                        val keep = value.toInt()
                        outerClass.get()?.switch_keep?.isChecked = keep == 1
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
