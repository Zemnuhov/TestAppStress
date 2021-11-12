package com.example.teststressappkotlin.Service

import android.R.attr
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import bleshadow.javax.inject.Inject
import com.example.teststressappkotlin.Constant
import java.util.*
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable

import com.polidea.rxandroidble2.RxBleDevice
import java.io.FileWriter
import android.R.attr.path
import android.annotation.SuppressLint
import android.app.Notification
import com.example.teststressappkotlin.NotificationModel
import com.opencsv.CSVWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


class BleService: Service() {
    private val binder = LocalBinder()
    companion object{
        //--------------Device UUID--------------//
        val notificationDataUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val writePeaksUUID: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
        val writeTonicUUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
        val writeTimeUUID: UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
    }



    //----------------BLEInstruments----------------//
    @Inject lateinit var device: RxBleDevice
    @Inject lateinit var connectionObservable: Observable<RxBleConnection>

    private var isRecoding = false
    private lateinit var writer:CSVWriter

    override fun onCreate() {
        super.onCreate()
        Constant.daggerObject!!.inject(this)
        setNotification()
        startForeground(1, NotificationModel().getForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setConnectionObserver()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getBleConnection(): Observable<RxBleConnection> {
        return device
            .establishConnection(true)
            .replay()
            .autoConnect()
    }

    private fun setNotification(){
        val disposable = connectionObservable
            .flatMap { it.setupNotification(notificationDataUUID) }
            .flatMap { it }
            .map { bytes -> ByteBuffer.wrap(bytes).int }
            .subscribe{ println(it)}

    }

    private fun intToByteArray(value: Int): ByteArray? {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun setConnectionObserver(){
        val disposable = device.observeConnectionStateChanges().subscribe{
            Log.i("ConnectionState", it.toString())
            if(it.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){
                connectionObservable = getBleConnection()
                if(isRecoding){
                    stopRecoding()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun startRecoding(){
        isRecoding = true
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm:ss")
        writer = CSVWriter(FileWriter("Egor "+dateFormat.format(Date())))


    }

     fun stopRecoding(){
        isRecoding = false
        writer.writeNext(arrayOf("0","0","0","0"))
        writer.close()
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }
}