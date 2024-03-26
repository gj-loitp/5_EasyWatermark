package com.mckimquyen.watermark.data.model

import android.graphics.Bitmap
import androidx.annotation.Keep
import com.mckimquyen.watermark.data.repo.UserConfigRepository

@Keep
data class UserPreferences(
    val outputFormat: Bitmap.CompressFormat,
    val compressLevel: Int,
) {
    companion object {
        val DEFAULT = UserPreferences(
            UserConfigRepository.DEFAULT_BITMAP_COMPRESS_FORMAT,
            UserConfigRepository.DEFAULT_COMPRESS_LEVEL
        )
    }
}
