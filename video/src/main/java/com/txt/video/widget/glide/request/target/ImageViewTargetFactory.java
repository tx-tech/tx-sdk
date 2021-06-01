package com.txt.video.widget.glide.request.target;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.txt.video.widget.glide.load.resource.drawable.GlideDrawable;
import com.txt.video.widget.glide.request.target.BitmapImageViewTarget;
import com.txt.video.widget.glide.request.target.DrawableImageViewTarget;
import com.txt.video.widget.glide.request.target.GlideDrawableImageViewTarget;
import com.txt.video.widget.glide.request.target.Target;

/**
 * A factory responsible for producing the correct type of {@link Target} for a given
 * {@link android.view.View} subclass.
 */
public class ImageViewTargetFactory {

    @SuppressWarnings("unchecked")
    public <Z> Target<Z> buildTarget(ImageView view, Class<Z> clazz) {
        if (GlideDrawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new GlideDrawableImageViewTarget(view);
        } else if (Bitmap.class.equals(clazz)) {
            return (Target<Z>) new BitmapImageViewTarget(view);
        } else if (Drawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new DrawableImageViewTarget(view);
        } else {
            throw new IllegalArgumentException("Unhandled class: " + clazz
                    + ", try .as*(Class).transcode(ResourceTranscoder)");
        }
    }
}
