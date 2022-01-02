package com.example.teststressappkotlin.Service

import android.R.attr
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

import com.example.teststressappkotlin.Constant
import java.util.*
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable

import com.polidea.rxandroidble2.RxBleDevice
import java.io.FileWriter
import android.R.attr.path
import android.annotation.SuppressLint
import android.app.Notification
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.teststressappkotlin.Dager.AppModule
import com.example.teststressappkotlin.Model.KalmanFilter
import com.example.teststressappkotlin.NotificationModel
import com.jjoe64.graphview.series.DataPoint
import com.opencsv.CSVWriter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BleService: Service() {
    private val binder = LocalBinder()
    companion object{
        //--------------Device UUID--------------//
        val notificationDataUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val writePeaksUUID: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
        val writeTonicUUID: UUID = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb")
        val writeTimeUUID: UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
    }


    private var SHARED_PREFERENCES_TAG = "STRESS_APP"
    //----------------BLEInstruments----------------//

    //@Inject lateinit var connectionObservable: Observable<RxBleConnection>

    private var isRecoding = false
    private lateinit var writer:CSVWriter

    val compositeDisposable:CompositeDisposable = CompositeDisposable()

    private var pastValue = 0.0
    var k = 0.1 // коэффициент фильтрации, 0.0-1.0
    var filVal = 0.0

    var isPeaks = false

    override fun onCreate() {
        super.onCreate()
        val appCompanent = Constant.daggerObject
        appCompanent?.inject(this)
        if (Constant.connection==null){
            Constant.connection = getBleConnection()
        }
        setNotification()
        setConnectionObserver()
        startForeground(1, NotificationModel().getForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /** Получение нового соединения. Вызывается в случае разрыва с устройством.
     *  Устройство соединения не изменяется. **/
    private fun getBleConnection(): Observable<RxBleConnection> {
        return Constant.device!!
            .establishConnection(true)
            .replay()
            .autoConnect()
    }

    private fun setNotification(){
        Log.e("BLEServiceConnection", Constant.connection.toString())
        val kalman = KalmanFilter(1.0, 1.0, 2.0, 40.0)
        kalman.setState(0.0, 0.1)
        var isFirst = true
        val disposable = Constant.connection!!
            .subscribeOn(Schedulers.computation())
            .flatMap { rxBleConnection -> rxBleConnection.setupNotification(BleService.notificationDataUUID) }
            .flatMap { it }
            .map { ByteBuffer.wrap(it).int }
            .map { Constant.convertValue(it) }
            .subscribe({
                //Log.e("NotificationData",it.toString())
                val tonic = it
                val graph:Double  = expRunningAverage(kalman.correct(peaksConvert(it.toDouble())))

                if(graph>1.25 && !isPeaks){
                    isPeaks = true
                }

                if (graph<1.25 && isPeaks){
                    isPeaks = false
                    savePeaks(loadPeaks()+1)

                }
                //[Время, Тоника, ЗначениеГрафика, ПикИлиНет]
                if (isRecoding) {
                    writer.writeNext(
                        arrayOf(
                            Date().time.toString(),
                            tonic.toString(),
                            graph.toString(),
                            isPeaks.toString()
                        )
                    )
                }
            },
                {
                    Log.e("Service_Notification",it.message.toString())
                }
            )
        compositeDisposable.add(disposable)
    }
    private fun peaksConvert(beginValue: Double): Double {
        val value: Double = beginValue - pastValue
        pastValue = beginValue
        return value
    }


    // Фильтрация (бегущее среднее)
    private fun expRunningAverage(newVal: Double): Double {
        filVal += (newVal - filVal) * k
        return filVal
    }


    private fun intToByteArray(value: Int): ByteArray? {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun setConnectionObserver(){
        val disposable = Constant.device!!.observeConnectionStateChanges()
            .subscribe(
            {
                Log.i("ConnectionState1", it.toString())
                if(it.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){

                    Constant.connection = getBleConnection()
                    stopRecoding()
                    setNotification()
                }
            },
            {
                Log.e("Connection",it.message.toString())
            })
    }



    fun startRecoding(){
        if (!isRecoding){
            isRecoding = true
            val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm:ss")
            val path = getExternalFilesDir("files")
            val file = File(path,"Egor"+dateFormat.format(Date())+".csv")
            file.createNewFile()
            writer = CSVWriter(FileWriter(file.absoluteFile))
            savePeaks(0)
        }else{
            Toast.makeText(Constant.context,"Запись уже запущена",Toast.LENGTH_SHORT).show()
        }


    }

     fun stopRecoding(){
         if (isRecoding){
             isRecoding = false
             writer.close()
         }else{
             //Toast.makeText(Constant.context,"Запись уже остановлена",Toast.LENGTH_SHORT).show()
         }

    }

    private fun savePeaks(peak: Int) {
        val sPref = Constant.context!!.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
        val ed = sPref.edit()
        ed.putInt("Peaks", peak)
        ed.apply()
    }

    private fun loadPeaks(): Int {
        val sPref = Constant.context!!.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
        return sPref.getInt("Peaks", 0)
    }


    override fun onBind(intent: Intent?): IBinder {

        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService = this@BleService
    }
}