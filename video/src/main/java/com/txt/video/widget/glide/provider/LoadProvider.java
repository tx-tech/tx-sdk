package com.txt.video.widget.glide.provider;

import com.txt.video.widget.glide.load.model.ModelLoader;
import com.txt.video.widget.glide.load.resource.transcode.ResourceTranscoder;
import com.txt.video.widget.glide.provider.DataLoadProvider;

/**
 * An extension of {@link com.txt.video.widget.glide.provider.DataLoadProvider} that also allows a
 * {@link ModelLoader} and a
 * {@link ResourceTranscoder} to be retrieved.
 *
 * @param <A> The type of model.
 * @param <T> The type of data that will be decoded from.
 * @param <Z> The type of resource that will be decoded.
 * @param <R> The type of resource that the decoded resource will be transcoded to.
 */
public interface LoadProvider<A, T, Z, R> extends DataLoadProvider<T, Z> {

    /**
     * Returns the {@link ModelLoader} to convert from the given model to a data type.
     */
    ModelLoader<A, T> getModelLoader();

    /**
     * Returns the {@link ResourceTranscoder} to convert from the decoded
     * and transformed resource into the transcoded resource.
     */
    ResourceTranscoder<Z, R> getTranscoder();
}
