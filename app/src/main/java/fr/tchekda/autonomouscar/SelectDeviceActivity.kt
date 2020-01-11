package fr.tchekda.autonomouscar

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.select_device_layout.*
import org.jetbrains.anko.toast

class SelectDeviceActivity : AppCompatActivity() {

    val TAG: String = "SelectDeviceActivity"
    val REQUEST_ENABLE_BLUETOOTH = 1

    var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var paired_devices: Set<BluetoothDevice>

    companion object {
        val EXTRA_ADDRESS = "Device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_device_layout)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            toast("Device doesn't support Bluetooth")
            return
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }else {
            pairedDeviceList()
        }

        button_bt_refresh.setOnClickListener { pairedDeviceList() }
    }

    private fun pairedDeviceList() {
        paired_devices = bluetoothAdapter!!.bondedDevices
        var list: ArrayList<BluetoothDevice> = ArrayList()

        if (!paired_devices.isEmpty()) {
            paired_devices.forEach {
                list.add(it)
                Log.i("Device", it.address + " : " + it.name)
            }
        }else {
            toast("No paired devices found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        list_devices.adapter = adapter

        list_devices.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            Log.i("Select", "Clicked")
            val device: BluetoothDevice = list[position]
            val address: String = device.address

            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            Log.i("Select", "Starting")
            startActivity(intent)
            Log.i("Select", "Started")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (bluetoothAdapter!!.isEnabled) {
                            toast("Bluetooth has been enabled")
                            pairedDeviceList()
                        }else {
                            toast("Bluetooth has been disabled")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        toast("Bluetooth enabling has been canceled")
                    }
                }
            }
        }
    }
}
