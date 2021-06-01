package com.txt.video.widget.glide.load.engine;

import com.txt.video.widget.glide.load.Key;

interface EngineJobListener {

    void onEngineJobComplete(Key key, EngineResource<?> resource);

    void onEngineJobCancelled(EngineJob engineJob, Key key);
}
