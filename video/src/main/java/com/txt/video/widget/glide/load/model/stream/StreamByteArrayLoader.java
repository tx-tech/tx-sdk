package com.txt.video.widget.glide.load.model.stream;

import android.content.Context;

import com.txt.video.widget.glide.load.data.ByteArrayFetcher;
import com.txt.video.widget.glide.load.data.DataFetcher;
import com.txt.video.widget.glide.load.model.GenericLoaderFactory;
import com.txt.video.widget.glide.load.model.ModelLoader;
import com.txt.video.widget.glide.load.model.ModelLoaderFactory;
import com.txt.video.widget.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

/**
 * A base class to convert byte arrays to input streams so they can be decoded. This class is abstract because there is
 * no simple/quick way to generate an id from the bytes themselves, so subclass must include an id.
 */
public class StreamByteArrayLoader implements StreamModelLoader<byte[]> {
    private final String id;

    public StreamByteArrayLoader() {
        this("");
    }

    /**
     * @deprecated Use {@link com.txt.video.widget.glide.GenericRequestBuilder#signature(com.txt.video.widget.glide.load.Key)}
     * and the empty constructor instead. Scheduled to be removed in Glide 4.0.
     */
    @Deprecated
    public StreamByteArrayLoader(String id) {
        this.id = id;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(byte[] model, int width, int height) {
        return new ByteArrayFetcher(model, id);
    }

    /**
     * Factory for {@link StreamByteArrayLoader}.
     */
    public static class Factory implements ModelLoaderFactory<byte[], InputStream> {

        @Override
        public ModelLoader<byte[], InputStream> build(Context context, GenericLoaderFactory factories) {
            return new StreamByteArrayLoader();
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }
}
