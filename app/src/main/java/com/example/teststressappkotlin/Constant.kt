package com.example.teststressappkotlin

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentManager
import com.example.teststressappkotlin.Dager.AppComponent
import com.polidea.rxandroidble2.ClientComponent
import com.polidea.rxandroidble2.DaggerClientComponent
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable


@SuppressLint("StaticFieldLeak")
object Constant {
    var context: Context? = null
    var daggerObject: AppComponent? = null
    var fragmentManager : FragmentManager? = null
    var connection: Observable<RxBleConnection>? = null
    var device: RxBleDevice? = null

    fun convertValue(value: Int): Int {
        return value * 10000 / 1023
    }


}