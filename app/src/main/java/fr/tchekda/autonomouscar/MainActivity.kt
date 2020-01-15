package fr.tchekda.autonomouscar

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var handler: Handler

    init {
        val outerClass = WeakReference(this)
        handler = MyHandler(outerClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val socketThread = SocketThread()
        socketThread.start()
        socketThread.setHandler(handler)

        button_test.setOnClickListener {
            Thread {
                socketThread.sendMessage("Salut mon pote")
            }.start()
        }
    }

    class MyHandler(private val outerClass: WeakReference<MainActivity>) : Handler() {

        override fun handleMessage(msg: Message?) {
            outerClass.get()?.text_message?.text = msg?.obj.toString()
        }
    }
}
