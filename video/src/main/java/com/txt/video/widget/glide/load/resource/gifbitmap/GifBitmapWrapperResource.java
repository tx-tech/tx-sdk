package com.txt.video.widget.glide.load.resource.gifbitmap;

import android.graphics.Bitmap;

import com.txt.video.widget.glide.load.engine.Resource;
import com.txt.video.widget.glide.load.resource.gif.GifDrawable;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapper;

/**
 * A resource that wraps an {@link GifBitmapWrapper}.
 */
public class GifBitmapWrapperResource implements Resource<GifBitmapWrapper> {
    private final GifBitmapWrapper data;

    public GifBitmapWrapperResource(GifBitmapWrapper data) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        this.data = data;
    }

    @Override
    public GifBitmapWrapper get() {
        return data;
    }

    @Override
    public int getSize() {
        return data.getSize();
    }

    @Override
    public void recycle() {
        Resource<Bitmap> bitmapResource = data.getBitmapResource();
        if (bitmapResource != null) {
            bitmapResource.recycle();
        }
        Resource<GifDrawable> gifDataResource = data.getGifResource();
        if (gifDataResource != null) {
            gifDataResource.recycle();
        }
    }
}
