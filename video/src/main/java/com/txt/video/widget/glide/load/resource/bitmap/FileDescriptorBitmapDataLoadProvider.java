package com.txt.video.widget.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import com.txt.video.widget.glide.load.DecodeFormat;
import com.txt.video.widget.glide.load.Encoder;
import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.ResourceEncoder;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.resource.NullEncoder;
import com.txt.video.widget.glide.load.resource.bitmap.BitmapEncoder;
import com.txt.video.widget.glide.load.resource.bitmap.FileDescriptorBitmapDecoder;
import com.txt.video.widget.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.txt.video.widget.glide.load.resource.file.FileToStreamDecoder;
import com.txt.video.widget.glide.provider.DataLoadProvider;

import java.io.File;

/**
 * An {@link com.txt.video.widget.glide.provider.DataLoadProvider} that provides classes for decoding and encoding
 * {@link Bitmap}s from {@link ParcelFileDescriptor} data.
 */
public class FileDescriptorBitmapDataLoadProvider implements DataLoadProvider<ParcelFileDescriptor, Bitmap> {
    private final ResourceDecoder<File, Bitmap> cacheDecoder;
    private final FileDescriptorBitmapDecoder sourceDecoder;
    private final BitmapEncoder encoder;
    private final Encoder<ParcelFileDescriptor> sourceEncoder;

    public FileDescriptorBitmapDataLoadProvider(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        cacheDecoder = new FileToStreamDecoder<Bitmap>(new StreamBitmapDecoder(bitmapPool, decodeFormat));
        sourceDecoder = new FileDescriptorBitmapDecoder(bitmapPool, decodeFormat);
        encoder = new BitmapEncoder();
        sourceEncoder = NullEncoder.get();
    }

    @Override
    public ResourceDecoder<File, Bitmap> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<ParcelFileDescriptor, Bitmap> getSourceDecoder() {
        return sourceDecoder;
    }

    @Override
    public Encoder<ParcelFileDescriptor> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<Bitmap> getEncoder() {
        return encoder;
    }
}
