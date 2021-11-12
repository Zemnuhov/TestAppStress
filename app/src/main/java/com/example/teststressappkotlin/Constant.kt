package com.example.teststressappkotlin

import android.annotation.SuppressLint
import android.content.Context
import com.example.teststressappkotlin.Dager.AppComponent
import com.polidea.rxandroidble2.ClientComponent
import com.polidea.rxandroidble2.DaggerClientComponent


@SuppressLint("StaticFieldLeak")
object Constant {
    var context: Context? = null
    var daggerObject: AppComponent? = null


}