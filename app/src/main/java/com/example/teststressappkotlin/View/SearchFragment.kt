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
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleConnection.RxBleConnectionState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class SearchFragment: Fragment(), SearchAdapter.CallBack {
    private lateinit var recyclerView:RecyclerView
    private var devices: ArrayList<Device> = ArrayList()
    private var devicesMAC: ArrayList<String> = ArrayList()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var search_button: Button
    private val compositeDisposable:CompositeDisposable

    @Inject
    lateinit var bleDevice: RxBleDevice

    init {
        Constant.daggerObject!!.inject(this)
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
        val disposable = bleDevice.observeConnectionStateChanges()
            .observeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bleState: RxBleConnectionState ->
                Log.i("ConnectionState", bleState.toString())
                if (bleState == RxBleConnectionState.CONNECTED) {
                    getMainFragment()
                }
            }
        val timerDisposable = Observable.timer(10,TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                disposable.dispose()
                Toast.makeText(context,"Соединение не удалось!",Toast.LENGTH_LONG).show()
            }
        compositeDisposable.add(disposable)
    }

    override fun clickItem() {
        setConnectionListener()
        if(bleDevice.connectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
            getMainFragment()
        }
    }

    private fun getMainFragment(){
        childFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment()).commit()
    }


}