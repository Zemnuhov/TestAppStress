package com.example.teststressappkotlin.View

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teststressappkotlin.Constant
import com.example.teststressappkotlin.Device
import com.example.teststressappkotlin.R
import com.example.teststressappkotlin.Settings.SettingsPresenter

class SearchAdapter(var devices: ArrayList<Device>):
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    lateinit var callBack: CallBack

    interface CallBack{
        fun clickItem()
    }

    public fun registerCallBack(callBack: CallBack){
        this.callBack = callBack
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameView: TextView
        val macView: TextView
        init {
            nameView = itemView.findViewById(R.id.name_text_view)
            macView= itemView.findViewById(R.id.mac_text_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(Constant.context)
            .inflate(R.layout.device_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameView.text = devices[position].name
        holder.macView.text = devices[position].MAC
        holder.itemView.setOnClickListener{
            SettingsPresenter().saveDevice(devices[position].MAC)
            callBack.clickItem()
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}