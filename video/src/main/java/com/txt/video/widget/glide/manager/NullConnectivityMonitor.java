package com.txt.video.widget.glide.manager;

import com.txt.video.widget.glide.manager.ConnectivityMonitor;

/**
 * A no-op {@link ConnectivityMonitor}.
 */
class NullConnectivityMonitor implements ConnectivityMonitor {

    @Override
    public void onStart() {
        // Do nothing.
    }

    @Override
    public void onStop() {
        // Do nothing.
    }

    @Override
    public void onDestroy() {
        // Do nothing.
    }
}
