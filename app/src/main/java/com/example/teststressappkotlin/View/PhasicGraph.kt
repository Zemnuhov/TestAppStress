package com.example.teststressappkotlin.View

import android.graphics.Color
import android.util.Log
import android.view.View
import com.example.teststressappkotlin.Constant
import com.example.teststressappkotlin.Model.KalmanFilter
import com.example.teststressappkotlin.R
import com.example.teststressappkotlin.Service.BleService
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhasicGraph(mainView: View) {

    private var graph: GraphView
    //@Inject
    //lateinit var connection: Observable<RxBleConnection>
    private var pastValue = 0.0
    var k = 0.01 // коэффициент фильтрации, 0.0-1.0
    var filVal = 0.0

    private var normalSeries: LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf<DataPoint>())
    private var peaksSeries: PointsGraphSeries<DataPoint> = PointsGraphSeries(arrayOf<DataPoint>())
    val compositeDisposable = CompositeDisposable()

    lateinit var connectDisposable:Disposable



    private fun getBleConnection(): Observable<RxBleConnection> {
        return Constant.device!!
            .establishConnection(true)
            .replay()
            .autoConnect()
    }


    init {
        if (Constant.connection==null){
            Constant.connection = getBleConnection()
        }
        graph = mainView.findViewById(R.id.main_graph) as GraphView
        settingGraph()
        setConnectionObserver()
        setBleListener()
    }


    private fun settingGraph() {
        graph.addSeries(normalSeries)
        graph.addSeries(peaksSeries)
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = false
        graph.viewport.setMinY(-3.0)
        graph.viewport.setMaxY(3.0)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(15000.0)
        graph.viewport.isScalable = true
        graph.viewport.isScrollable = true
        graph.viewport.setScalableY(false)
        graph.viewport.setScrollableY(false)
        graph.setBackgroundColor(Color.WHITE)
        graph.gridLabelRenderer.gridColor = Color.WHITE
        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
        graph.gridLabelRenderer.isVerticalLabelsVisible = false
        graph.gridLabelRenderer.setHumanRounding(false)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(Constant.context)
        graph.gridLabelRenderer.numHorizontalLabels = 3 // only 4 because of the space
        peaksSeries.color = Color.RED
        peaksSeries.size = 3f
        normalSeries.color = Color.BLACK
    }

    //Заполнение графика, запуск фильтра Калмана
    private fun setBleListener(){
        Log.e("PhasicGraphConnection", Constant.connection.toString())
        val kalman = KalmanFilter(1.0, 1.0, 2.0, 40.0)
        kalman.setState(0.0, 0.1)
        connectDisposable = Constant.connection!!
            .subscribeOn(Schedulers.computation())
            .delay(2000,TimeUnit.MILLISECONDS)
            .flatMap { rxBleConnection -> rxBleConnection.setupNotification(BleService.notificationDataUUID) }
            .flatMap { it }
            .map { ByteBuffer.wrap(it).int }
            .map { Constant.convertValue(it) }
            .map { peaksConvert(it.toDouble()) }
            .map { expRunningAverage(it.toDouble()) }
            .map { kalman.correct(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.e("PhaseData", it.toString())
                    val dataPoint = DataPoint(Date(), it)
                    normalSeries.appendData(dataPoint, true, 10000)
                },
                {
                    Log.e("ConnectionState2", it.toString())
                }
            )
    }

    private fun setConnectionObserver(){
        val disposable = Constant.device!!.observeConnectionStateChanges()
            .subscribeOn(Schedulers.computation())
            .subscribe(
            {
                Log.i("ConnectionState2", it.toString())
                if(it.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
                    connectDisposable.dispose()
                    setBleListener()
                }
                if (it.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){

                }
            },
            {
                Log.i("ConnectionState2", it.toString())
            })
    }

    private fun peaksConvert(beginValue: Double): Double {
        val value: Double = beginValue - pastValue
        pastValue = beginValue
        return value
    }


    // Фильтрация (бегущее среднее)
    fun expRunningAverage(newVal: Double): Double {
        filVal += (newVal - filVal) * k
        return filVal
    }
}