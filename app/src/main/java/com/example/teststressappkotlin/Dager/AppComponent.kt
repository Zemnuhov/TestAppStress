package com.example.teststressappkotlin.Dager

import com.example.teststressappkotlin.MainActivity
import com.example.teststressappkotlin.Service.BleService
import com.example.teststressappkotlin.View.MainFragment
import com.example.teststressappkotlin.View.PhasicGraph
import com.example.teststressappkotlin.View.SearchFragment
import dagger.Component
import javax.inject.Singleton
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(searchFragment: SearchFragment)
    fun inject(bleService: BleService)
    fun inject(mainActivity: MainActivity)
    fun inject(mainFragment: MainFragment)
    fun inject(phasicGraph: PhasicGraph)
}