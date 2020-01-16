package fr.tchekda.autonomouscar

import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException

class SocketThread(private val dev: Boolean = false) : Thread() {

    private lateinit var handler: Handler
    var writer: PrintWriter? = null

    override fun run() {
        super.run()
        val address = if (dev) "192.168.43.101" else "192.168.43.98"
        val socket: Socket
        try {
            socket = Socket(address, 2020)
        }catch (e: ConnectException) {
            e.printStackTrace()
            return
        }
        val input = socket.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        var text: String
        val output = socket.getOutputStream()
        writer = PrintWriter(output, true)
        while (true) {
            try {
                text = reader.readLine()
                Log.i("Socket", text)
                handler.sendMessage(handler.obtainMessage(0, text))
            }catch (e: SocketException){
                e.printStackTrace()
                break
            }
        }
    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun sendMessage(data: Map<String, String>) {
        var message = ""
        data.forEach { (key, value) ->
            message += "$key=$value|"
        }
        writer?.println(message)
    }

    override fun destroy() {
    }
}