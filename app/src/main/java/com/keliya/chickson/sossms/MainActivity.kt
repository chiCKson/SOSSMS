package com.keliya.chickson.sossms

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.telephony.SmsManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.Manifest.permission.SEND_SMS
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.os.Build



class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 0
    private val DEVICE_ADDRESS = "98:D3:37:90:B1:92"
    private val PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")//Serial Port Service ID
    private var device: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    internal var deviceConnected = false
    internal var thread: Thread? = null
    internal var buffer: ByteArray?=null
    internal var bufferPosition: Int = 0
    internal var stopThread: Boolean = false
    var phoneNo:String?=null
    var message:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener { v ->

            if(BTinit()) {
                if (BTconnect()) {
                    deviceConnected = true
                    beginListenForData()
                    tv1.append("\nConnection Opened!\n")
                }
            }
        }
        button2.setOnClickListener { v ->




        }

    }

    fun BTinit(): Boolean {
        var found = false
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext, "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableAdapter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableAdapter, 0)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Toast.makeText(applicationContext, e.toString(),
                        Toast.LENGTH_LONG).show()
            }

        }
        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bondedDevices.isEmpty()) {
            Toast.makeText(applicationContext, "Please Pair the Device first", Toast.LENGTH_SHORT).show()
        } else {
            for (iterator in bondedDevices) {
                if (iterator.address == DEVICE_ADDRESS) {
                    device = iterator
                    found = true
                    break
                }
            }
        }
        return found
    }

    fun BTconnect(): Boolean {
        var connected = true
        try {
            socket = device!!.createRfcommSocketToServiceRecord(PORT_UUID)
            socket!!.connect()
        } catch (e: IOException) {
            e.printStackTrace()
            connected = false
        }

        if (connected) {
            try {
                outputStream = socket!!.getOutputStream()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                inputStream = socket!!.getInputStream()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


        return connected
    }

    internal fun beginListenForData() {
        val handler = Handler()
        stopThread = false
        buffer = ByteArray(1024)
        val thread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted && !stopThread) {
                try {
                    val byteCount = inputStream!!.available()
                    if (byteCount > 0) {
                        val rawBytes = ByteArray(byteCount)
                        inputStream!!.read(rawBytes)
                        val string = String(rawBytes)
                        handler.post {
                            tv1.text=string
                            if(string=="1"){
                                sendSMSMessage()
                            }
                        }


                    }
                } catch (ex: IOException) {
                    stopThread = true
                }

            }
        })

        thread.start()
    }
    @SuppressWarnings("deprecation")
    protected fun sendSMSMessage() {
        phoneNo = editText2.getText().toString()
        message = "Help me I'm in trouble Please call me. Thank you"

        //ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS.toInt())ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS.toInt())
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, message, null, null)
            Toast.makeText(applicationContext, "SMS Sent!",
                    Toast.LENGTH_LONG).show()
        } catch (e: Exception) {

            Toast.makeText(applicationContext,
                    "SMS faild, please try again later!"+e.toString(),
                    Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }
}
