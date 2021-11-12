package com.example.teststressappkotlin.Service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.teststressappkotlin.Constant

class ServicePresenter {
    lateinit var bleService: BleService
    var isConnection = false
    val context = Constant.context

    val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleService.LocalBinder
            bleService = binder.getService()
            isConnection = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnection = false
        }
    }

    fun connectService(){
        Intent(context, BleService::class.java).also { intent ->
            context!!.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun disconnectService(){
        context!!.unbindService(serviceConnection)
    }

    fun startRecoding() = bleService.startRecoding()
    fun stopRecoding() = bleService.stopRecoding()
}