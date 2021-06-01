package com.txt.video.widget.glide.load.resource.bitmap;

import android.graphics.Bitmap;

import com.txt.video.widget.glide.load.DecodeFormat;
import com.txt.video.widget.glide.load.Encoder;
import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.ResourceEncoder;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.model.StreamEncoder;
import com.txt.video.widget.glide.load.resource.bitmap.BitmapEncoder;
import com.txt.video.widget.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.txt.video.widget.glide.load.resource.file.FileToStreamDecoder;
import com.txt.video.widget.glide.provider.DataLoadProvider;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link com.txt.video.widget.glide.provider.DataLoadProvider} that provides decoders and encoders for decoding and caching
 * {@link Bitmap}s using {@link InputStream} data.
 */
public class StreamBitmapDataLoadProvider implements DataLoadProvider<InputStream, Bitmap> {
    private final StreamBitmapDecoder decoder;
    private final BitmapEncoder encoder;
    private final StreamEncoder sourceEncoder;
    private final FileToStreamDecoder<Bitmap> cacheDecoder;

    public StreamBitmapDataLoadProvider(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        sourceEncoder = new StreamEncoder();
        decoder = new StreamBitmapDecoder(bitmapPool, decodeFormat);
        encoder = new BitmapEncoder();
        cacheDecoder = new FileToStreamDecoder<Bitmap>(decoder);
    }

    @Override
    public ResourceDecoder<File, Bitmap> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<InputStream, Bitmap> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<InputStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<Bitmap> getEncoder() {
        return encoder;
    }
}
