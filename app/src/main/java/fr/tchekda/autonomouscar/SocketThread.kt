package fr.tchekda.autonomouscar

import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SocketThread(private val dev: Boolean = false) : Thread() {

    private lateinit var handler: Handler
    private lateinit var socket: Socket
    private lateinit var executor: ExecutorService

    var writer: PrintWriter? = null

    override fun run() {
        super.run()
        val address = if (dev) "192.168.0.48" else "192.168.43.98"
        val port = 2020
        Log.i("Socket", "Initializing $address:$port")
        handler.sendMessage(handler.obtainMessage(0, "connect=0|"))
        try {
            socket = Socket(address, port)
        }catch (e: ConnectException) {
            Log.i("Socket", "Connection to $address:$port failed")
            //e.printStackTrace()
            handler.sendMessage(handler.obtainMessage(0, "connect=0|"))
            return
        }catch (e: NoRouteToHostException){
            Log.i("Socket", "Can't find $address, Host unreachable")
            //e.printStackTrace()
            handler.sendMessage(handler.obtainMessage(0, "connect=0|"))
            return
        }
        Log.i("Socket", "Connection to $address:$port succeeded")
        handler.sendMessage(handler.obtainMessage(0, "connect=1|"))

        executor = Executors.newFixedThreadPool(1)

        val input = socket.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))
        var text: String
        val output = socket.getOutputStream()
        writer = PrintWriter(output, true)
        while (true) {
            try {
                text = reader.readLine()
                Log.i("Socket","Received : $text")
                handler.sendMessage(handler.obtainMessage(0, text))
            }catch (e: SocketException){
                Log.i("Socket", "Connection to $address:$port has been reset")
                e.printStackTrace()
                break
            }catch (e: IllegalStateException){
                Log.i("Socket", "Connection to $address:$port has crashed")
                e.printStackTrace()
                break
            }
        }

        disconnect()

    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun disconnect(){
        if (::socket.isInitialized){
            if (!socket.isClosed) {
                executor.shutdownNow()
                while (!executor.isTerminated) {
                }
                socket.close()
                Log.i("Socket", "Socket disconnected")
                handler.sendMessage(handler.obtainMessage(0, "connect=0|"))
            }
        }
    }

    fun sendMessage(data: Map<String, String>) {
        if (::socket.isInitialized) {
            if (!socket.isClosed) {
                val worker = Runnable {
                    var message = ""
                    data.forEach { (key, value) ->
                        message += "$key=$value|"
                    }
                    writer?.println(message)
                }
                executor.execute(worker)
            }
        }

    }

    override fun destroy() {
        Log.i("Socket", "Terminating Socket Thread")
        disconnect()
        handler.sendMessage(handler.obtainMessage(0, "connect=0|"))
    }
}