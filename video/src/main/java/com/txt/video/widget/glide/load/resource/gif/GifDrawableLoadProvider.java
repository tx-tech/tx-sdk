package com.txt.video.widget.glide.load.resource.gif;

import android.content.Context;

import com.txt.video.widget.glide.load.Encoder;
import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.ResourceEncoder;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.model.StreamEncoder;
import com.txt.video.widget.glide.load.resource.file.FileToStreamDecoder;
import com.txt.video.widget.glide.load.resource.gif.GifDrawable;
import com.txt.video.widget.glide.load.resource.gif.GifResourceDecoder;
import com.txt.video.widget.glide.load.resource.gif.GifResourceEncoder;
import com.txt.video.widget.glide.provider.DataLoadProvider;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link com.txt.video.widget.glide.provider.DataLoadProvider} that loads an {@link InputStream} into
 * {@link GifDrawable} that can be used to display an animated GIF.
 */
public class GifDrawableLoadProvider implements DataLoadProvider<InputStream, GifDrawable> {
    private final GifResourceDecoder decoder;
    private final GifResourceEncoder encoder;
    private final StreamEncoder sourceEncoder;
    private final FileToStreamDecoder<GifDrawable> cacheDecoder;

    public GifDrawableLoadProvider(Context context, BitmapPool bitmapPool) {
        decoder = new GifResourceDecoder(context, bitmapPool);
        cacheDecoder = new FileToStreamDecoder<GifDrawable>(decoder);
        encoder = new GifResourceEncoder(bitmapPool);
        sourceEncoder = new StreamEncoder();
    }

    @Override
    public ResourceDecoder<File, GifDrawable> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<InputStream, GifDrawable> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<InputStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<GifDrawable> getEncoder() {
        return encoder;
    }
}
