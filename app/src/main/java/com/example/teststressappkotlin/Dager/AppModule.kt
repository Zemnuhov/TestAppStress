package com.example.teststressappkotlin.Dager

import com.example.teststressappkotlin.Constant
import com.example.teststressappkotlin.Settings.SettingsPresenter
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable


@Module
class AppModule {

    @Provides
    @Singleton
    fun getBleDevice(): RxBleDevice{
        val rxBleClient = RxBleClient.create(Constant.context!!)
        return rxBleClient.getBleDevice(SettingsPresenter().getDevice()!!)
    }

}