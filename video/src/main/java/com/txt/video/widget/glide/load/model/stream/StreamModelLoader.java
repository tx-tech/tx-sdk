package com.txt.video.widget.glide.load.model.stream;

import com.txt.video.widget.glide.load.model.ModelLoader;

import java.io.InputStream;

/**
 * A base class for {@link ModelLoader}s that translate models into {@link InputStream}s.
 *
 * @param <T> The type of the model that will be translated into an {@link InputStream}.
 */
public interface StreamModelLoader<T> extends ModelLoader<T, InputStream> {
    // specializing the generic arguments
}
