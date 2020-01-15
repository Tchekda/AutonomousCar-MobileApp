package fr.tchekda.autonomouscar

import android.os.Handler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketThread:Thread() {

    private lateinit var handler: Handler
    var writer : PrintWriter? = null

    override fun run() {
        super.run()
        val socket = Socket("192.168.43.98", 2020)
        val input = socket.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        var text: String
        val output = socket.getOutputStream()
        writer = PrintWriter(output, true)
        while (true){
            text = reader.readLine()
            handler.sendMessage(handler.obtainMessage(0, text))
        }
    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun sendMessage(string: String) {
        writer?.println(string)
    }

    override fun destroy() {

    }
}