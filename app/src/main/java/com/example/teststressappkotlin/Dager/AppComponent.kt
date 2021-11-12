package com.example.teststressappkotlin.Dager

import com.example.teststressappkotlin.MainActivity
import com.example.teststressappkotlin.Service.BleService
import com.example.teststressappkotlin.View.SearchFragment
import dagger.Component
import javax.inject.Singleton
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(searchFragment: SearchFragment)
    fun inject(BleService: BleService)
    fun inject(mainActivity: MainActivity)
}