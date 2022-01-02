package com.example.teststressappkotlin.View

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.teststressappkotlin.Constant
import com.example.teststressappkotlin.Model.KalmanFilter
import com.example.teststressappkotlin.R
import com.example.teststressappkotlin.Service.BleService
import com.example.teststressappkotlin.Service.ServicePresenter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import com.opencsv.CSVWriter
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileWriter
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class MainFragment: Fragment() {
    private var servicePresenter: ServicePresenter = ServicePresenter()
    private lateinit var startRecodingButton: Button
    private lateinit var stopRecodingButton: Button
    private lateinit var saveKeepButton: Button
    private lateinit var keepEditText: EditText
    private lateinit var hourEditText: EditText
    private lateinit var minuteEditText: EditText
    lateinit var mainView: View
    private lateinit var peaksTextView: TextView
    private lateinit var tonicTextView: TextView
    lateinit var graph: PhasicGraph
    var SHARED_PREFERENCES_TAG = "STRESS_APP"
    lateinit var disposable: Disposable

    init {
        Constant.daggerObject!!.inject(this)
        servicePresenter.connectService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainView = inflater.inflate(R.layout.main_fragment,container,false)
        graph = PhasicGraph(mainView)
        initViews()
        startBleService()
        setConnectionObserver()
        return mainView
    }

    private fun initViews(){
        startRecodingButton = mainView.findViewById(R.id.start_rec )!!
        stopRecodingButton = mainView.findViewById(R.id.stop_rec)!!
        saveKeepButton = mainView.findViewById(R.id.save_keep)!!
        keepEditText = mainView.findViewById(R.id.keep)
        tonicTextView = mainView.findViewById(R.id.tonic_value)
        hourEditText = mainView.findViewById(R.id.hour)
        minuteEditText = mainView.findViewById(R.id.minute)
        peaksTextView = mainView.findViewById(R.id.peaks_value)
        setListeners()
        setTonicListener()
    }


    private fun setListeners(){
        startRecodingButton.setOnClickListener{
            servicePresenter.startRecoding()
        }
        stopRecodingButton.setOnClickListener{
            servicePresenter.stopRecoding()
        }
        saveKeepButton.setOnClickListener{
            val keep = keepEditText.text.toString()
            savingKeep(keep)
        }
    }

    fun getTime() : Calendar{
        var time = Calendar.getInstance()
        if(hourEditText.text.length==2&&minuteEditText.text.length == 2) {
            time.set(Calendar.HOUR_OF_DAY, hourEditText.text.toString().toInt())
            time.set(Calendar.MINUTE, minuteEditText.text.toString().toInt())
            return time
        }
        return time
    }

    fun savingKeep(keep: String){
        val path = context?.getExternalFilesDir("files")
        var file = File(path,"keep.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.appendText(getTime().time.time.toString()+" "+keep+"\n")


    }

    private fun startBleService(){
        val intent = Intent(Constant.context, BleService.javaClass)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Constant.context!!.startForegroundService(intent)
        }
        else{
            Constant.context!!.startService(intent)
        }
    }

    fun setTonicListener(){
        disposable = Constant.connection!!.subscribeOn(Schedulers.io())
            .flatMap { rxBleConnection -> rxBleConnection.setupNotification(BleService.notificationDataUUID) }
            .flatMap { it }
            .map { ByteBuffer.wrap(it).int }
            .map { Constant.convertValue(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (
                {
                    tonicTextView.text = it.toString()
                    val peaks = loadPeaks().toString()
                    peaksTextView.text = peaks
                },
                {
                    Log.e("TonicListener",it.message.toString())
                })
    }

    private fun setConnectionObserver(){
        val disposable = Constant.device!!.observeConnectionStateChanges()
            .subscribeOn(Schedulers.computation())
            .subscribe(
                {
                    Log.i("ConnectionState2", it.toString())
                    if(it.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
                        disposable.dispose()
                        setTonicListener()
                    }
                    if (it.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){

                    }
                },
                {
                    Log.i("ConnectionState2", it.toString())
                })
    }

    fun loadPeaks():Int {
        val sPref = Constant.context!!.getSharedPreferences(SHARED_PREFERENCES_TAG,
            Service.MODE_PRIVATE
        )
        val peaksCount = sPref.getInt("Peaks", 0)
        return peaksCount
    }






}