package com.example.teststressappkotlin.View

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject

class MainFragment: Fragment() {
    private var servicePresenter: ServicePresenter = ServicePresenter()
    private lateinit var startRecodingButton: Button
    private lateinit var stopRecodingButton: Button
    private lateinit var graph: GraphView
    @Inject lateinit var connection: Observable<RxBleConnection>
    private var pastValue = 0.0

    private val normalSeries = LineGraphSeries(arrayOf<DataPoint>())
    private val peaksSeries = PointsGraphSeries(arrayOf<DataPoint>())

    init {
        servicePresenter.connectService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment,container,false)

        startBleService()
        initViews()
        settingGraph()
        setBleListener()

        return view
    }

    private fun initViews(){
        graph = view?.findViewById(R.id.main_graph)!!
        startRecodingButton = view?.findViewById(R.id.start_rec )!!
        stopRecodingButton = view?.findViewById(R.id.stop_rec)!!
        setListeners()
    }

    private fun setListeners(){
        startRecodingButton.setOnClickListener{
            servicePresenter.startRecoding()
        }
        stopRecodingButton.setOnClickListener{
            servicePresenter.stopRecoding()
        }
    }

    private fun startBleService(){
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Constant.context!!.startForegroundService(intent)
        }
        else{
            Constant.context!!.startService(intent)
        }
    }

    fun setBleListener(){
        val kalman = KalmanFilter(1.0, 1.0, 2.0, 15.0)
        kalman.setState(0.0, 0.1)
        var isFirst = true
        val disposable = connection
            .subscribeOn(Schedulers.computation())
            .flatMap { rxBleConnection -> rxBleConnection.setupNotification(BleService.notificationDataUUID) }
            .flatMap { notificationObservable -> notificationObservable }
            .map { ByteBuffer.wrap(it).int }
            .map { Constant.convertValue(it) }
            .map { peaksConvert(it.toDouble()) }
            .subscribe{
                if(isFirst){
                    kalman.setState(it, 0.1)
                    isFirst = false
                }
                val dataPoint = DataPoint(Date(),it)
                peaksSeries.appendData(dataPoint, true, 10000)
            }
    }

    private fun peaksConvert(beginValue: Double): Double {
        val value: Double = beginValue - pastValue
        pastValue = beginValue
        return value
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
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity)
        graph.gridLabelRenderer.numHorizontalLabels = 3 // only 4 because of the space
        peaksSeries.color = Color.RED
        peaksSeries.size = 3f
        normalSeries.color = Color.BLACK
    }
}