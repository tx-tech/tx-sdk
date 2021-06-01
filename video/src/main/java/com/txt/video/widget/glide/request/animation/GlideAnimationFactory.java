package com.txt.video.widget.glide.request.animation;

import com.txt.video.widget.glide.request.animation.GlideAnimation;

/**
 * A factory class that can produce different {@link GlideAnimation}s based on the
 * state of the request.
 * @param <R> The type of resource that needs to be animated into the target.
 */
public interface GlideAnimationFactory<R> {

    /**
     * Returns a new {@link GlideAnimation}.
     *
     * @param isFromMemoryCache True if this will be an animation for a resource that was loaded from the memory cache.
     * @param isFirstResource True if this is the first resource to be loaded into the target.
     */
    GlideAnimation<R> build(boolean isFromMemoryCache, boolean isFirstResource);
}
