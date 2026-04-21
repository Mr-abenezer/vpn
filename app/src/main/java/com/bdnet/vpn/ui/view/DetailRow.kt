package com.bdnet.vpn.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.bdnet.vpn.R

class DetailRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.DetailRow)
            val label = typedArray.getString(R.styleable.DetailRow_label) ?: ""
            val value = typedArray.getString(R.styleable.DetailRow_value) ?: ""
            typedArray.recycle()

            inflate(context, R.layout.detail_row, this)

            findViewById<TextView>(R.id.label_text).text = label
            findViewById<TextView>(R.id.value_text).text = value
        }
    }
}
