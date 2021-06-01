package com.txt.video.net.bean;



import com.txt.video.widget.adapter.base.entity.AbstractExpandableItem;
import com.txt.video.widget.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/31.
 * description：
 */
public class LevelItem1 extends AbstractExpandableItem<FileBean.ListBean> implements MultiItemEntity {

    private List<MultiItemEntity> childNode;
    private String title;

    public void setChildNode(List<MultiItemEntity> childNode) {
        this.childNode = childNode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LevelItem1(List<MultiItemEntity> childNode, String title) {
        this.childNode = childNode;
        this.title = title;
    }

    public LevelItem1(List<MultiItemEntity> childNode) {
        this.childNode = childNode;
    }



    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
