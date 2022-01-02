package com.example.teststressappkotlin

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.teststressappkotlin.Dager.DaggerAppComponent
import com.example.teststressappkotlin.Settings.SettingsPresenter
import com.example.teststressappkotlin.View.MainFragment
import com.example.teststressappkotlin.View.SearchFragment
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import java.sql.Connection
import javax.inject.Inject


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Constant.context = applicationContext
        if (Constant.daggerObject == null) {
            Constant.daggerObject = DaggerAppComponent.builder().build()
        }
        Constant.fragmentManager = supportFragmentManager
        Constant.daggerObject?.inject(this)
        if (Constant.device==null) {
            val rxBleClient = RxBleClient.create(Constant.context!!)
            Constant.device = rxBleClient.getBleDevice(SettingsPresenter().getDevice()!!)
        }

        val permissionStatus = ContextCompat
            .checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        if(permissionStatus == PackageManager.PERMISSION_GRANTED){
            startApp()
        }else{
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
    }

    private fun startApp(){
        Log.e("Connection",Constant.device!!.connectionState.toString())
        val device = Constant.device
        if(Constant.device!!.connectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SearchFragment())
                .commit()
        }
    }
}