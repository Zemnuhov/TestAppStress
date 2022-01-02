package com.example.teststressappkotlin.View

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teststressappkotlin.*
import com.example.teststressappkotlin.Service.BleService
import com.example.teststressappkotlin.Settings.SettingsPresenter
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleConnection.RxBleConnectionState
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit


class SearchFragment: Fragment(), SearchAdapter.CallBack {
    private lateinit var recyclerView:RecyclerView
    private var devices: ArrayList<Device> = ArrayList()
    private var devicesMAC: ArrayList<String> = ArrayList()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var search_button: Button
    private val compositeDisposable:CompositeDisposable


    init {

        compositeDisposable = CompositeDisposable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.search_fragment, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)!!
        search_button = view.findViewById(R.id.search_button)!!
        searchAdapter = SearchAdapter(devices)
        searchAdapter.registerCallBack(this)
        recyclerView.adapter = searchAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        onClick()
        return view
    }

    private fun onClick(){
        search_button.setOnClickListener{
            val rxBleClient: RxBleClient = RxBleClient.create(requireContext())
            var disposable = rxBleClient.scanBleDevices(ScanSettings.Builder().build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    val device = Device(it.bleDevice.name.toString(),it.bleDevice.macAddress)
                    if (device.MAC !in devicesMAC) {
                        devicesMAC.add(device.MAC)
                        devices.add(device)
                        searchAdapter = SearchAdapter(devices)
                        searchAdapter.registerCallBack(this)
                        recyclerView.adapter = searchAdapter
                    }
                }
            compositeDisposable.add(disposable)
        }
    }

    private fun setConnectionListener(){
        //Log.i("ConnectionState", bleDevice.connectionState.toString())
        if (Constant.device!!.connectionState.equals(RxBleConnectionState.CONNECTED)){
            getMainFragment()
        }

        val notificationConnection = Constant.connection!!
            .flatMap { it.setupNotification(BleService.notificationDataUUID) }
            .flatMap {it}
            .subscribe(
                {
                    //System.out.println(it)
                },
                {
                    Log.e("ConnectionListener",it.message.toString())
                })




        val disposable = Constant.device!!.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //Log.i("ConnectionState", it.toString())
                if (it.equals(RxBleConnectionState.CONNECTED)) {
                    getMainFragment()
                }
            }

        val timerDisposable = Observable.timer(10,TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                disposable.dispose()
                if (Constant.device!!.connectionState
                        .equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                    //Toast.makeText(context, "Соединение не удалось!", Toast.LENGTH_LONG).show()
                }
            }
        compositeDisposable.add(disposable)
    }

    override fun clickItem() {
        Constant.device = getDevice()
        Constant.connection = getBleConnection()
        setConnectionListener()
        if(Constant.device!!.connectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
            getMainFragment()
        }
    }


    private fun getMainFragment(){
        Constant.fragmentManager!!.beginTransaction()
            .replace(R.id.container, MainFragment()).commit()
    }

    fun getDevice(): RxBleDevice{
        val rxBleClient = RxBleClient.create(Constant.context!!)
        return rxBleClient.getBleDevice(SettingsPresenter().getDevice()!!)
    }

    fun getBleConnection(): Observable<RxBleConnection> {
        return Constant.device!!
            .establishConnection(true)
            .replay()
            .autoConnect()
    }




}