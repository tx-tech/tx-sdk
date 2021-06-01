package com.txt.video.widget.glide.manager;

import com.txt.video.widget.glide.manager.LifecycleListener;

/**
 * An interface for listening to Activity/Fragment lifecycle events.
 */
public interface Lifecycle {
    /**
     * Adds the given listener to the set of listeners managed by this Lifecycle implementation.
     */
    void addListener(LifecycleListener listener);
}
