package com.mckimquyen.watermark.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.watermark.R
import com.mckimquyen.watermark.data.model.TextPaintStyle
import com.mckimquyen.watermark.data.model.TextTypeface
import com.mckimquyen.watermark.ui.base.BaseViewHolder
import com.mckimquyen.watermark.utils.ktx.colorOnPrimary

class TextTypefaceAdapter(
    private val dataList: ArrayList<TextTypefaceModel>,
    initTypeface: TextTypeface? = TextTypeface.Normal,
    initTextStyle: TextPaintStyle? = TextPaintStyle.Fill,
    private val onClickAction: (pos: Int, typeface: TextTypeface) -> Unit = { _, _ -> },
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPos: Int
    private var textPaintStyle: TextPaintStyle

    init {
        selectedPos = dataList.indexOfFirst { it.textTypeface == initTypeface }.coerceAtLeast(0)
        textPaintStyle = initTextStyle ?: TextPaintStyle.Fill
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.item_typeface_style, parent, false)

        return TypefaceHolder(
            root
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        handleView(holder, position)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        handleView(holder, position)
    }

    private fun handleView(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val model = dataList[position]
        val selected = position == selectedPos
        with(holder as TypefaceHolder) {
            tvPreview.apply {
                model.textTypeface.applyStyle(this)
            }
            tvTitle?.text = model.title
            tvPreview.setTextColor(
                if (selected) {
                    tvPreview.context.colorOnPrimary
                } else {
                    tvPreview.context.colorOnPrimary
                }
            )
            textPaintStyle.applyStyle(tvPreview)
            tvPreview.setOnClickListener {
                onClickAction.invoke(position, model.textTypeface)
                updateSelected(position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTextStyle(textPaintStyle: TextPaintStyle) {
        this.textPaintStyle = textPaintStyle
        notifyDataSetChanged()
    }

    private fun updateSelected(pos: Int) {
        if (pos == selectedPos) {
            return
        }
        notifyItemChanged(selectedPos, "Selected")
        selectedPos = pos
        notifyItemChanged(selectedPos, "Selected")
    }

    internal class TypefaceHolder(val root: View) : BaseViewHolder(root) {
        val tvPreview: TextView by lazy {
            root.findViewById(R.id.tvPreview)
        }
        val tvTitle: TextView? by lazy {
            root.findViewById(R.id.tvTitle)
        }
    }

    data class TextTypefaceModel(
        val textTypeface: TextTypeface = TextTypeface.Normal,
        val title: String,
    )

    companion object {
        fun obtainDefaultTypefaceList(context: Context): ArrayList<TextTypefaceModel> {
            return arrayListOf(
                TextTypefaceModel(
                    textTypeface = TextTypeface.Normal,
                    title = context.getString(R.string.text_typeface_normal)
                ),
                TextTypefaceModel(
                    textTypeface = TextTypeface.Bold,
                    title = context.getString(R.string.text_typeface_bold)
                ),
                TextTypefaceModel(
                    textTypeface = TextTypeface.Italic,
                    title = context.getString(R.string.text_typeface_italic)
                ),
                TextTypefaceModel(
                    textTypeface = TextTypeface.BoldItalic,
                    title = context.getString(R.string.text_typeface_bold_italic)
                ),
            )
        }
    }
}
