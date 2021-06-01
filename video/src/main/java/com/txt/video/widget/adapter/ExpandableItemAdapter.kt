package com.txt.video.widget.adapter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import com.txt.video.R
import com.txt.video.widget.utils.DatetimeUtil
import com.txt.video.net.bean.FileBean
import com.txt.video.net.bean.LevelItem1
import com.txt.video.net.utils.TxLogUtils
import com.txt.video.widget.adapter.base.BaseMultiItemQuickAdapter
import com.txt.video.widget.adapter.base.BaseViewHolder
import com.txt.video.widget.adapter.base.entity.MultiItemEntity
import com.txt.video.widget.glide.TxGlide
import java.text.ParseException

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class ExpandableItemAdapter(var data: ArrayList<MultiItemEntity>) :
    BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(
        data
    ) {
    val TYPE_LEVEL_0 = 0
    val TYPE_LEVEL_1 = 1

    init {
        addItemType(TYPE_LEVEL_0, R.layout.tx_layout_item_head)
        addItemType(TYPE_LEVEL_1, R.layout.tx_adapter_file_list_item)

    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            TYPE_LEVEL_0 -> {
                val levelItem1 = item as LevelItem1
                helper.setText(R.id.tx_tv_headtitle, levelItem1.title)
                if (levelItem1.isExpanded) {
                    helper.setImageResource(R.id.tx_iv_arrow, R.drawable.tx_icon_bottom_arrow)
                } else {
                    helper.setImageResource(R.id.tx_iv_arrow, R.drawable.tx_icon_right_arrow)
                }
                helper.itemView.setOnClickListener {
                    val adapterPosition = helper.adapterPosition
                    if (levelItem1.isExpanded) {
                        collapse(adapterPosition)
                    } else {
                        expand(adapterPosition)
                    }
                }
            }
            else -> {
                val listBean = item as FileBean.ListBean
                val name: String = listBean.name

                helper.setText(R.id.tx_tv_file_name, name)
                try {
                    val split = name.split("\\.".toRegex()).toTypedArray()
                    val suffix = split[1]
                    TxLogUtils.i("suffix--$suffix")
                    if (suffix == "ppt" || suffix == "pptx") {
                        helper.setImageResource(R.id.tx_iv_file_pic, R.drawable.tx_ppt)
                    } else if (suffix == "pdf") {
                        helper.setImageResource(R.id.tx_iv_file_pic, R.drawable.tx_icon_pdf)
                    } else if (suffix == "word" || suffix == "docx") {
                        helper.setImageResource(R.id.tx_iv_file_pic, R.drawable.tx_icon_word)
                    } else {
                        val view = helper.getView<ImageView>(R.id.tx_iv_file_pic)

                        TxGlide.with(view.context).load(listBean.url)
                            .placeholder(R.drawable.tx_pic_default)
                            .into(view)

//                baseViewHolder.setImageResource(R.id.tx_iv_file_pic, R.drawable.tx_pic_default)

                    }
                } catch (e: Exception) {
                }

                if (null != listBean.isUploading) {
                    val view = helper.getView<RelativeLayout>(R.id.tx_ll_iv_file_pic)
                    if (listBean.isUploading) {
                        view.visibility=  View.VISIBLE
                        val ivUploading = helper.getView<ImageView>(R.id.tx_ll_iv_uploading)
                        val ofFloat = ObjectAnimator.ofFloat(ivUploading, "rotation", 0f, 359f)
                        ofFloat.duration = 2000
                        ofFloat.repeatCount = ValueAnimator.INFINITE
                        ofFloat.interpolator = LinearInterpolator()
                        ofFloat.start()
                    }else{
                        view.visibility=  View.GONE

                    }

                }

                try {
                    helper.setText(
                        R.id.tx_tv_file_time,
                        DatetimeUtil.UTCToCST(listBean.getCtime())
                    )
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }


}