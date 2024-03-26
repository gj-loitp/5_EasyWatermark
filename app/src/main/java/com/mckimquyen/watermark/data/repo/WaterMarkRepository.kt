package com.mckimquyen.watermark.data.repo

import android.graphics.Color
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import androidx.collection.ArrayMap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mckimquyen.watermark.MyApp
import com.mckimquyen.watermark.R
import com.mckimquyen.watermark.data.model.ImageInfo
import com.mckimquyen.watermark.data.model.TextPaintStyle
import com.mckimquyen.watermark.data.model.TextTypeface
import com.mckimquyen.watermark.data.model.WaterMark
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_ALPHA
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_DEGREE
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_ENABLE_BOUNDS
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_HORIZON_GAP
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_ICON_URI
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_MODE
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_TEXT
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_TEXT_COLOR
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_TEXT_SIZE
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_TEXT_STYLE
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_TEXT_TYPEFACE
import com.mckimquyen.watermark.data.repo.WaterMarkRepository.PreferenceKeys.KEY_VERTICAL_GAP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WaterMarkRepository @Inject constructor(
    @Named("WaterMarkPreferences") private val dataStore: DataStore<Preferences>,
) {

    private object PreferenceKeys {
        val KEY_TEXT = stringPreferencesKey(SP_KEY_TEXT)
        val KEY_TEXT_SIZE = floatPreferencesKey(SP_KEY_TEXT_SIZE)
        val KEY_TEXT_COLOR = intPreferencesKey(SP_KEY_TEXT_COLOR)
        val KEY_TEXT_STYLE = intPreferencesKey(SP_KEY_TEXT_STYLE)
        val KEY_TEXT_TYPEFACE = intPreferencesKey(SP_KEY_TEXT_TYPEFACE)
        val KEY_ALPHA = intPreferencesKey(SP_KEY_ALPHA)
        val KEY_HORIZON_GAP = intPreferencesKey(SP_KEY_HORIZON_GAP)
        val KEY_VERTICAL_GAP = intPreferencesKey(SP_KEY_VERTICAL_GAP)
        val KEY_DEGREE = floatPreferencesKey(SP_KEY_DEGREE)
        val KEY_ICON_URI = stringPreferencesKey(SP_KEY_ICON_URI)

        //        val KEY_URI = stringPreferencesKey(SP_KEY_URI)
        val KEY_MODE = intPreferencesKey(SP_KEY_WATERMARK_MODE)
        val KEY_ENABLE_BOUNDS = booleanPreferencesKey(SP_KEY_ENABLE_BOUNDS)
//        val KEY_TILE_MODE = intPreferencesKey(SP_KEY_TILE_MODEL)
//        val KEY_OFFSET_X = floatPreferencesKey(SP_KEY_OFFSET_X)
//        val KEY_OFFSET_Y = floatPreferencesKey(SP_KEY_OFFSET_Y)
    }

    private val _selectedImage = MutableStateFlow(ImageInfo.empty())

    val selectedImage: StateFlow<ImageInfo> = _selectedImage

    val waterMark: Flow<WaterMark> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            WaterMark(
                text = it[KEY_TEXT] ?: MyApp.instance.getString(R.string.config_default_water_mark_text),
                textSize = (it[KEY_TEXT_SIZE] ?: 14f).coerceAtLeast(1f),
                textColor = it[KEY_TEXT_COLOR] ?: Color.parseColor("#FFB800"),
                textStyle = TextPaintStyle.obtainSealedClass(it[KEY_TEXT_STYLE] ?: 0),
                textTypeface = TextTypeface.obtainSealedClass(it[KEY_TEXT_TYPEFACE] ?: 0),
                alpha = it[KEY_ALPHA] ?: 255,
                degree = it[KEY_DEGREE] ?: 315f,
                hGap = it[KEY_HORIZON_GAP] ?: 0,
                vGap = it[KEY_VERTICAL_GAP] ?: 0,
                iconUri = Uri.parse(it[KEY_ICON_URI] ?: ""),
                markMode = if (it[KEY_MODE] == MarkMode.Image.value) MarkMode.Image else MarkMode.Text,
                enableBounds = it[KEY_ENABLE_BOUNDS] ?: false
            )
        }

    private val _imageMapFlow: MutableStateFlow<List<ImageInfo>> = MutableStateFlow(emptyList())
    private val imageInfoMap: MutableMap<Uri, Int> = ArrayMap(_imageMapFlow.value.size)

    val imageInfoMapFlow = _imageMapFlow

    val imageInfoList: List<ImageInfo>
        get() = imageInfoMapFlow.value

    suspend fun updateImageList(imageList: List<ImageInfo>) {
        val map = imageList.mapIndexed { index, imageInfo -> imageInfo.uri to index }.toMap()
        imageInfoMap.clear()
        imageInfoMap.putAll(map)
        _imageMapFlow.emit(imageList)
    }

    suspend fun updateText(text: String) {
        dataStore.edit {
            it[KEY_MODE] = MarkMode.Text.value
            it[KEY_TEXT] = text
        }
    }

    suspend fun updateTextSize(size: Float) {
        dataStore.edit { it[KEY_TEXT_SIZE] = size }
    }

    suspend fun updateColor(color: Int) {
        dataStore.edit { it[KEY_TEXT_COLOR] = color }
    }

    suspend fun updateTextStyle(style: TextPaintStyle) {
        dataStore.edit { it[KEY_TEXT_STYLE] = style.serializeKey() }
    }

    suspend fun updateTypeFace(typeface: TextTypeface) {
        dataStore.edit { it[KEY_TEXT_TYPEFACE] = typeface.serializeKey() }
    }

    suspend fun updateAlpha(alpha: Int) {
        dataStore.edit { it[KEY_ALPHA] = alpha.coerceAtLeast(0).coerceAtMost(255) }
    }

    suspend fun updateHorizon(gap: Int) {
        dataStore.edit { it[KEY_HORIZON_GAP] = gap.coerceAtLeast(0).coerceAtMost(MAX_HORIZON_GAP) }
    }

    suspend fun updateVertical(gap: Int) {
        dataStore.edit {
            it[KEY_VERTICAL_GAP] = gap.coerceAtLeast(0).coerceAtMost(MAX_VERTICAL_GAP)
        }
    }

    suspend fun updateDegree(degree: Float) {
        dataStore.edit { it[KEY_DEGREE] = degree.coerceAtLeast(0f).coerceAtMost(MAX_DEGREE) }
    }

    suspend fun updateIcon(iconUri: Uri) {
        dataStore.edit {
            it[KEY_MODE] = MarkMode.Image.value
            it[KEY_ICON_URI] = iconUri.toString()
        }
    }

    suspend fun updateTileMode(imageInfo: ImageInfo, mode: Shader.TileMode): ImageInfo {
        if (imageInfo.tileMode == mode.ordinal) {
            Log.i("WaterMarkRepository", "updateTileMode: same mode")
            return imageInfo
        }
        val index = imageInfoMap[imageInfo.uri] ?: kotlin.run {
            Log.e("WaterMarkRepository", "updateTileMode: imageInfo not found, uri = ${imageInfo.uri}")
            return imageInfo
        }

        val info = imageInfo.copy(tileMode = mode.ordinal)
        val list = ArrayList(imageInfoList)
        list[index] = info
        imageInfoMap[info.uri] = index
        _imageMapFlow.emit(list)
        _selectedImage.emit(info)
        return info
    }

    suspend fun updateOffset(imageInfo: ImageInfo) {
        if (imageInfo == selectedImage.value) {
            return
        }
        val index = imageInfoMap[selectedImage.value.uri] ?: kotlin.run {
//            Log.e("WaterMarkRepository", "updateOffset: imageInfo not found, uri = ${selectedImage.value.uri}")
            return
        }
        val list = ArrayList(imageInfoList)
        list[index] = imageInfo
        imageInfoMap[imageInfo.uri] = index
        _imageMapFlow.emit(list)
        _selectedImage.emit(imageInfo)
    }

    suspend fun resetModeToText() {
        dataStore.edit { it[KEY_MODE] = MarkMode.Text.value }
    }

    suspend fun toggleBounds(enable: Boolean) {
        dataStore.edit { it[KEY_ENABLE_BOUNDS] = enable }
    }

//    suspend fun resetList() {
//        updateImageList(emptyList())
//    }

    suspend fun select(uri: Uri) = withContext(Dispatchers.Default) {
        val info = imageInfoList.find { it.uri == uri } ?: ImageInfo(uri)
        _selectedImage.emit(info)
    }

    sealed class MarkMode(val value: Int) {
        object Text : MarkMode(0)

        object Image : MarkMode(1)
    }

    companion object {
        const val SP_NAME = "sp_water_mark_config"
        const val SP_KEY_TEXT = "${SP_NAME}_key_text"
        const val SP_KEY_TEXT_SIZE = "${SP_NAME}_key_text_size"
        const val SP_KEY_TEXT_COLOR = "${SP_NAME}_key_text_color"
        const val SP_KEY_TEXT_STYLE = "${SP_NAME}_key_text_style"
        const val SP_KEY_TEXT_TYPEFACE = "${SP_NAME}_key_text_typeface"
        const val SP_KEY_ALPHA = "${SP_NAME}_key_alpha"
        const val SP_KEY_HORIZON_GAP = "${SP_NAME}_key_horizon_gap"
        const val SP_KEY_VERTICAL_GAP = "${SP_NAME}_key_vertical_gap"
        const val SP_KEY_DEGREE = "${SP_NAME}_key_degree"
        const val SP_KEY_CHANGE_LOG = "${SP_NAME}_key_change_log"
        const val SP_KEY_ENABLE_BOUNDS = "${SP_NAME}_key_enable_bounds"
        const val SP_KEY_ICON_URI = "${SP_NAME}_key_icon_uri"
//        const val SP_KEY_URI = "${SP_NAME}_key_uri"
        const val SP_KEY_WATERMARK_MODE = "${SP_NAME}_key_watermark_mode"
//        const val SP_KEY_IMAGE_ROTATION = "${SP_NAME}_key_watermark_mode"
//        const val SP_KEY_TILE_MODEL = "${SP_NAME}_key_tile_model"
//        const val SP_KEY_OFFSET_X = "${SP_NAME}_key_offset_x"
//        const val SP_KEY_OFFSET_Y = "${SP_NAME}_key_offset_y"
        const val MAX_TEXT_SIZE = 100f
        const val MIN_TEXT_SIZE = 1f
        const val DEFAULT_TEXT_SIZE = 14f
        const val MAX_DEGREE = 360f
        const val MAX_HORIZON_GAP = 500
        const val MAX_VERTICAL_GAP = 500

    }
}
