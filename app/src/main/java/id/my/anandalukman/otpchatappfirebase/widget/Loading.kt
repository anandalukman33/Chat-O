package id.my.anandalukman.otpchatappfirebase.widget

import android.app.Activity
import android.app.AlertDialog
import id.my.anandalukman.otpchatappfirebase.R

class Loading (val mActivity: Activity) {

    private lateinit var isDialog: AlertDialog


    fun startLoading() {
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_progress_dialog, null)
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }


    fun dismissLoading() {
        isDialog.dismiss()
    }

}