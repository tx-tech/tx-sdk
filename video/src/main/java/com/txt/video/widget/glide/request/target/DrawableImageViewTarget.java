package com.txt.video.widget.glide.request.target;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.txt.video.widget.glide.request.target.ImageViewTarget;

/**
 * A target for display {@link Drawable} objects in {@link ImageView}s.
 */
public class DrawableImageViewTarget extends ImageViewTarget<Drawable> {
    public DrawableImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(Drawable resource) {
       view.setImageDrawable(resource);
    }
}
