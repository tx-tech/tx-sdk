package com.txt.video.widget.glide;

import com.txt.video.widget.glide.GenericRequestBuilder;

interface BitmapOptions {

    GenericRequestBuilder<?, ?, ?, ?> fitCenter();

    GenericRequestBuilder<?, ?, ?, ?> centerCrop();

}
