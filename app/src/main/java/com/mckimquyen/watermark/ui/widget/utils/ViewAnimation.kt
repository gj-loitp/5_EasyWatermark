package com.mckimquyen.watermark.ui.widget.utils

import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation

/**
 * Encapsulate View and SpringAnimation
 * @author roy.mobile.dev@gmail.com
 * @date 2021/8/12
 */
class ViewAnimation(
    val view: View,
    val animation: SpringAnimation? = null
) {

    init {
        animation?.addEndListener { _, _, _, _ ->
            listener?.applyAfterEnd(view, animation)
        }
    }

    private var listener: ViewAnimationListener? = null

    fun setListener(block: ViewAnimationListenerBuilder.() -> Unit) {
        listener = ViewAnimationListenerBuilder().also(block)
    }

    fun cancel() {
        animation?.cancel()
    }

    fun start() {
        listener?.applyBeforeStart(view, animation)
        animation?.start()
    }
}
