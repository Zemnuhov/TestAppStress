package com.example.teststressappkotlin.View

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.teststressappkotlin.Constant
import com.example.teststressappkotlin.R
import com.example.teststressappkotlin.Service.ServicePresenter

class MainFragment: Fragment() {
    var servicePresenter: ServicePresenter = ServicePresenter()
    lateinit var startRecodingButton: Button
    lateinit var stopRecodingButton: Button

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

        return view
    }

    fun initViews(){
        startRecodingButton = view?.findViewById(R.id.start_rec)!!
        stopRecodingButton = view?.findViewById(R.id.stop_rec)!!
    }

    fun startBleService(){
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Constant.context!!.startForegroundService(intent)
        }
        else{
            Constant.context!!.startService(intent)
        }
    }
}