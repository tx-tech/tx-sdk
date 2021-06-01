package com.txt.video.widget.adapter.base.diff;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.ListUpdateCallback;

import com.txt.video.widget.adapter.base.BaseQuickAdapter;


public final class BaseQuickAdapterListUpdateCallback implements ListUpdateCallback {

    @NonNull
    private final BaseQuickAdapter mAdapter;

    public BaseQuickAdapterListUpdateCallback(@NonNull BaseQuickAdapter adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public void onInserted(int position, int count) {
        this.mAdapter.notifyItemRangeInserted(position + mAdapter.getHeaderLayoutCount(), count);
    }

    @Override
    public void onRemoved(int position, int count) {
        this.mAdapter.notifyItemRangeRemoved(position + mAdapter.getHeaderLayoutCount(), count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        this.mAdapter.notifyItemMoved(fromPosition + mAdapter.getHeaderLayoutCount(), toPosition + mAdapter.getHeaderLayoutCount());
    }

    @Override
    public void onChanged(int position, int count, @Nullable Object payload) {
        this.mAdapter.notifyItemRangeChanged(position + mAdapter.getHeaderLayoutCount(), count, payload);
    }
}
