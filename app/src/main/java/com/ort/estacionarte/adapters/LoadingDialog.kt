package com.ort.estacionarte.adapters

import android.app.Activity
import android.app.AlertDialog
import com.ort.estacionarte.R

class LoadingDialog (private var activity: Activity ){

    private lateinit var dialog: AlertDialog

    fun startDialog() {
        var builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.progress_dialog,null))
        builder.setCancelable(false)

        dialog= builder.create()
        dialog.show()
    }

    fun dismissDialog(){
        dialog.dismiss()
    }

}