package com.txt.video.widget.glide.load.resource.gifbitmap;

import android.graphics.Bitmap;

import com.txt.video.widget.glide.load.Encoder;
import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.ResourceEncoder;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.model.ImageVideoWrapper;
import com.txt.video.widget.glide.load.resource.file.FileToStreamDecoder;
import com.txt.video.widget.glide.load.resource.gif.GifDrawable;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapper;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapperResourceDecoder;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapperResourceEncoder;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapperStreamResourceDecoder;
import com.txt.video.widget.glide.provider.DataLoadProvider;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link com.txt.video.widget.glide.provider.DataLoadProvider} that can load either an
 * {@link GifDrawable} or an {@link Bitmap} from either an
 * {@link InputStream} or an {@link android.os.ParcelFileDescriptor}.
 */
public class ImageVideoGifDrawableLoadProvider implements DataLoadProvider<ImageVideoWrapper, GifBitmapWrapper> {
    private final ResourceDecoder<File, GifBitmapWrapper> cacheDecoder;
    private final ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> sourceDecoder;
    private final ResourceEncoder<GifBitmapWrapper> encoder;
    private final Encoder<ImageVideoWrapper> sourceEncoder;

    public ImageVideoGifDrawableLoadProvider(DataLoadProvider<ImageVideoWrapper, Bitmap> bitmapProvider,
                                             DataLoadProvider<InputStream, GifDrawable> gifProvider, BitmapPool bitmapPool) {

        final GifBitmapWrapperResourceDecoder decoder = new GifBitmapWrapperResourceDecoder(
                bitmapProvider.getSourceDecoder(),
                gifProvider.getSourceDecoder(),
                bitmapPool
        );
        cacheDecoder = new FileToStreamDecoder<GifBitmapWrapper>(new GifBitmapWrapperStreamResourceDecoder(decoder));
        sourceDecoder = decoder;
        encoder = new GifBitmapWrapperResourceEncoder(bitmapProvider.getEncoder(), gifProvider.getEncoder());

        //TODO: what about the gif provider?
        sourceEncoder = bitmapProvider.getSourceEncoder();
    }

    @Override
    public ResourceDecoder<File, GifBitmapWrapper> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> getSourceDecoder() {
        return sourceDecoder;
    }

    @Override
    public Encoder<ImageVideoWrapper> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<GifBitmapWrapper> getEncoder() {
        return encoder;
    }
}
