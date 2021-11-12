package com.example.teststressappkotlin.Settings

class SettingsPresenter {
    var settingsModel = SettingsModel()

    fun saveDevice(MAC: String) {
        settingsModel.setSettings(settingsModel.DEVICE_ADDRESS_TAG, MAC)
    }

    fun getDevice(): String? {
        return settingsModel.getSettings(settingsModel.DEVICE_ADDRESS_TAG)
            ?: return "00:00:00:00:00:00"
    }

    fun getThreshold(): Double {
        return settingsModel.getSettings(settingsModel.THRESHOLD_TAG)!!.toDouble()
    }

    fun setThreshold(value: Double) {
        settingsModel.setSettings(settingsModel.THRESHOLD_TAG, value.toString())
    }
}