package com.txt.video.widget.glide.load.model;

import com.txt.video.widget.glide.load.data.DataFetcher;
import com.txt.video.widget.glide.load.model.GlideUrl;
import com.txt.video.widget.glide.load.model.ModelLoader;

import java.net.URL;

/**
 * A wrapper class that translates {@link URL} objects into {@link GlideUrl}
 * objects and then uses the wrapped {@link com.txt.video.widget.glide.load.model.ModelLoader} for
 * {@link GlideUrl}s to load the data.
 *
 * @param <T> The type of data that will be loaded from the {@link URL}s.
 */
public class UrlLoader<T> implements ModelLoader<URL, T> {
    private final ModelLoader<GlideUrl, T> glideUrlLoader;

    public UrlLoader(ModelLoader<GlideUrl, T> glideUrlLoader) {
        this.glideUrlLoader = glideUrlLoader;
    }

    @Override
    public DataFetcher<T> getResourceFetcher(URL model, int width, int height) {
        return glideUrlLoader.getResourceFetcher(new GlideUrl(model), width, height);
    }
}
