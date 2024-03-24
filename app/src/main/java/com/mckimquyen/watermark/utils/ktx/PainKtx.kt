package com.mckimquyen.watermark.utils.ktx

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import com.mckimquyen.watermark.data.model.ImageInfo
import com.mckimquyen.watermark.data.model.WaterMark

fun Paint.applyConfig(
    imageInfo: ImageInfo,
    config: WaterMark?,
    isScale: Boolean = true,
): Paint {
    val size = config?.textSize ?: 14f
    textSize = if (isScale) size else size * imageInfo.scaleX
    color = config?.textColor ?: Color.RED
    alpha = config?.alpha ?: 128
    style = config?.textStyle?.obtainSysStyle() ?: Paint.Style.FILL
    typeface =
        Typeface.create(typeface, config?.textTypeface?.obtainSysTypeface() ?: Typeface.NORMAL)
    isAntiAlias = true
    isDither = true
    textAlign = Paint.Align.CENTER
    // todo setShadowLayer(textSize / 2, 0f, 0f, color)
    return this
}

fun TextPaint.applyConfig(
    imageInfo: ImageInfo,
    config: WaterMark?,
    isScale: Boolean = true,
): TextPaint {
    return (this as Paint).applyConfig(imageInfo, config, isScale) as TextPaint
}
