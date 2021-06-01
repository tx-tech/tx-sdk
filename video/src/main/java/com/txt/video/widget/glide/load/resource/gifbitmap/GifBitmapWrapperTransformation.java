package com.txt.video.widget.glide.load.resource.gifbitmap;

import android.graphics.Bitmap;

import com.txt.video.widget.glide.load.Transformation;
import com.txt.video.widget.glide.load.engine.Resource;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.resource.gif.GifDrawable;
import com.txt.video.widget.glide.load.resource.gif.GifDrawableTransformation;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapper;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapperResource;

/**
 * A {@link com.txt.video.widget.glide.load.Transformation} that can apply a wrapped {@link Bitmap}
 * transformation to both {@link Bitmap}s and {@link GifDrawable}.
 */
public class GifBitmapWrapperTransformation implements Transformation<GifBitmapWrapper> {
    private final Transformation<Bitmap> bitmapTransformation;
    private final Transformation<GifDrawable> gifDataTransformation;

    public GifBitmapWrapperTransformation(BitmapPool bitmapPool, Transformation<Bitmap> bitmapTransformation) {
        this(bitmapTransformation, new GifDrawableTransformation(bitmapTransformation, bitmapPool));
    }

    GifBitmapWrapperTransformation(Transformation<Bitmap> bitmapTransformation,
                                   Transformation<GifDrawable> gifDataTransformation) {
        this.bitmapTransformation = bitmapTransformation;
        this.gifDataTransformation = gifDataTransformation;
    }

    @Override
    public Resource<GifBitmapWrapper> transform(Resource<GifBitmapWrapper> resource, int outWidth, int outHeight) {
        Resource<Bitmap> bitmapResource = resource.get().getBitmapResource();
        Resource<GifDrawable> gifResource = resource.get().getGifResource();
        if (bitmapResource != null && bitmapTransformation != null) {
            Resource<Bitmap> transformed = bitmapTransformation.transform(bitmapResource, outWidth, outHeight);
            if (!bitmapResource.equals(transformed)) {
                GifBitmapWrapper gifBitmap = new GifBitmapWrapper(transformed, resource.get().getGifResource());
                return new GifBitmapWrapperResource(gifBitmap);
            }
        } else if (gifResource != null && gifDataTransformation != null) {
            Resource<GifDrawable> transformed = gifDataTransformation.transform(gifResource, outWidth, outHeight);
            if (!gifResource.equals(transformed)) {
                GifBitmapWrapper gifBitmap = new GifBitmapWrapper(resource.get().getBitmapResource(), transformed);
                return new GifBitmapWrapperResource(gifBitmap);
            }
        }
        return resource;
    }

    @Override
    public String getId() {
        return bitmapTransformation.getId();
    }
}
