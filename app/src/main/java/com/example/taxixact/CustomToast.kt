package com.example.taxixact

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

object CustomToast {

    fun show(context: Context, message: String, textColor: Int, iconResId: Int, backgroundResId: Int) {

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setBackgroundResource(backgroundResId)
        layout.setPadding(16, 16, 16, 16)

        val icon = ImageView(context)
        icon.setImageResource(iconResId)
        icon.setPadding(8, 8, 0, 8)

        val textView = TextView(context)
        textView.text = message
        textView.setTextColor(textColor)
        textView.gravity = Gravity.CENTER
        textView.setPadding(8, 8, 8, 8)

        layout.addView(icon)
        layout.addView(textView)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 100) // Adjust position if necessary
        toast.show()
    }
}
