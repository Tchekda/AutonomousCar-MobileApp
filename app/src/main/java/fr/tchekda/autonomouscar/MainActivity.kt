package fr.tchekda.autonomouscar

import android.os.Bundle
import android.os.Handler
import android.os.Message
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

        socketThread = SocketThread(false)
        socketThread!!.start()
        socketThread!!.setHandler(handler)

        switch_dev.setOnCheckedChangeListener { _, isChecked ->
            socketThread!!.interrupt()
            socketThread = SocketThread(isChecked)

        }

        bar_speed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val finalSpeed = progress + 116
                data["speed"] = finalSpeed.toString()

                sendData()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })


    }

    fun sendData() {
        Thread {
            socketThread!!.sendMessage(data)
        }.start()
    }

    class MyHandler(private val outerClass: WeakReference<MainActivity>) : Handler() {

        override fun handleMessage(msg: Message?) {
            val receivePairs = msg?.obj.toString().split("|")
            val receiveData: MutableMap<String, String> = HashMap<String, String>()
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
                    }
                }
            }
             outerClass.get()?.receivedData = receiveData
        }
    }
}
