package com.example.teststressappkotlin.Settings

import android.content.Context
import android.content.SharedPreferences
import com.example.teststressappkotlin.Constant
import android.content.Context.MODE_PRIVATE
import android.util.Log


class SettingsModel {
    //------------TAGS------------//
    private val SHARED_PREFERENCES_TAG = "STRESS_APP"
    val DEVICE_ADDRESS_TAG = "DEVICE_ADDRESS"
    val STIMULUS_TAG = "STIMULUS"
    val SCHEDULE_TAG = "SCHEDULE"
    val THRESHOLD_TAG = "THRESHOLD"

    //------------Default Settings------------//
    private val DEFAULT_STIMULUS = "Семья:Работа:Друзья:Здоровье:Артефакты"
    private val DEFAULT_SCHEDULE = "Утро_8:00-11:59|День_12:00-17:59|Вечер_18:00-00:00"
    private val DEFAULT_THRESHOLD = "1.25"

    private var sPref: SharedPreferences? = null
    private var context: Context? = null

    init {
        context = Constant.context!!
        sPref = context!!.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE)
    }

    fun setSettings(TAG: String, setting: String) {
        val ed = sPref!!.edit()
        ed.putString(TAG, setting)
        ed.apply()
        Log.i("StressApp_SP", "$TAG $setting written")
    }

    fun getSettings(TAG: String): String? {
        val setting = sPref!!.getString(TAG, null)
        Log.i("StressApp_SP", "$TAG $setting reading")
        return setting
    }
}