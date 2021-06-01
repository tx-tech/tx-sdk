package com.txt.video.widget.adapter

import android.widget.ImageView
import com.txt.video.R
import com.txt.video.TXSdk
import com.txt.video.widget.adapter.base.BaseQuickAdapter
import com.txt.video.widget.adapter.base.BaseViewHolder
import com.txt.video.widget.glide.TxGlide

/**
 * Created by JustinWjq
 * @date 2020/9/3.
 * description：
 */
public class PicQuickAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.tx_adapter_pic_list_item, null){
    override fun convert(holder: BaseViewHolder, item: String) {
        val itemPosition = this.data.indexOf(item)
        val view = holder.getView<ImageView>(R.id.tx_imageView)
        holder.setText(R.id.tx_tv_count, "${itemPosition + 1}")

        TxGlide.with(TXSdk.getInstance().application).load(item).into(view)
    }

}