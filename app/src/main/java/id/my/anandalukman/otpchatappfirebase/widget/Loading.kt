package id.my.anandalukman.otpchatappfirebase.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import id.my.anandalukman.otpchatappfirebase.R

class Loading (context: Context, themeResId: Int) : Dialog(context, themeResId) {

    private var tvProgress : TextView? = null
    private var msgProgress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_progress_dialog)

        initUI()
        updateData()
    }

    private fun initUI() {
        tvProgress = findViewById(R.id.tv_progress)
    }

    private fun updateData() {
        tvProgress?.let { tvProgress?.setText(msgProgress) }
    }

    fun setMessage(msg: String) {
        msgProgress = msg
    }

    override fun show() {
        if (isShowing) {
            dismiss()
        }
        updateData()
        super.show()
    }

}