package com.txt.video.widget.glide.load.resource.gifbitmap;

import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.engine.Resource;
import com.txt.video.widget.glide.load.model.ImageVideoWrapper;
import com.txt.video.widget.glide.load.resource.gifbitmap.GifBitmapWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link com.txt.video.widget.glide.load.ResourceDecoder} that can decode an
 * {@link GifBitmapWrapper} from {@link InputStream} data.
 */
public class GifBitmapWrapperStreamResourceDecoder implements ResourceDecoder<InputStream, GifBitmapWrapper> {
    private final ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> gifBitmapDecoder;

    public GifBitmapWrapperStreamResourceDecoder(
            ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> gifBitmapDecoder) {
        this.gifBitmapDecoder = gifBitmapDecoder;
    }

    @Override
    public Resource<GifBitmapWrapper> decode(InputStream source, int width, int height) throws IOException {
        return gifBitmapDecoder.decode(new ImageVideoWrapper(source, null), width, height);
    }

    @Override
    public String getId() {
        return gifBitmapDecoder.getId();
    }
}
