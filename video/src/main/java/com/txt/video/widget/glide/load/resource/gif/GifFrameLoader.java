package com.txt.video.widget.glide.load.resource.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.txt.video.widget.glide.GenericRequestBuilder;
import com.txt.video.widget.glide.TxGlide;
import com.txt.video.widget.glide.load.Encoder;
import com.txt.video.widget.glide.load.Key;
import com.txt.video.widget.glide.load.Transformation;
import com.txt.video.widget.glide.load.engine.DiskCacheStrategy;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.resource.NullEncoder;
import com.txt.video.widget.glide.request.animation.GlideAnimation;
import com.txt.video.widget.glide.request.target.SimpleTarget;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.UUID;

class GifFrameLoader {

    private final FrameCallback callback;
    private final GifDecoder gifDecoder;
    private final Handler handler;

    private boolean isRunning = false;
    private boolean isLoadPending = false;
    private GenericRequestBuilder<GifDecoder, GifDecoder, Bitmap, Bitmap> requestBuilder;
    private DelayTarget current;
    private boolean isCleared;

    public interface FrameCallback {
        void onFrameReady(int index);
    }

    public GifFrameLoader(Context context, FrameCallback callback, GifDecoder gifDecoder, int width, int height) {
        this(callback, gifDecoder, null,
                getRequestBuilder(context, gifDecoder, width, height, TxGlide.get(context).getBitmapPool()));
    }

    GifFrameLoader(FrameCallback callback, GifDecoder gifDecoder, Handler handler,
                   GenericRequestBuilder<GifDecoder, GifDecoder, Bitmap, Bitmap> requestBuilder) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper(), new FrameLoaderCallback());
        }
        this.callback = callback;
        this.gifDecoder = gifDecoder;
        this.handler = handler;
        this.requestBuilder = requestBuilder;
    }

    @SuppressWarnings("unchecked")
    public void setFrameTransformation(Transformation<Bitmap> transformation) {
        if (transformation == null) {
            throw new NullPointerException("Transformation must not be null");
        }
        requestBuilder = requestBuilder.transform(transformation);
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        isCleared = false;

        loadNextFrame();
    }

    public void stop() {
        isRunning = false;
    }

    public void clear() {
        stop();
        if (current != null) {
            TxGlide.clear(current);
            current = null;
        }
        isCleared = true;
        // test.
    }

    public Bitmap getCurrentFrame() {
        return current != null ? current.getResource() : null;
    }

    private void loadNextFrame() {
        if (!isRunning || isLoadPending) {
            return;
        }
        isLoadPending = true;

        long targetTime = SystemClock.uptimeMillis() + gifDecoder.getNextDelay();
        gifDecoder.advance();
        DelayTarget next = new DelayTarget(handler, gifDecoder.getCurrentFrameIndex(), targetTime);
        requestBuilder
                .signature(new FrameSignature())
                .into(next);
    }

    // Visible for testing.
    void onFrameReady(DelayTarget delayTarget) {
        if (isCleared) {
            handler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, delayTarget).sendToTarget();
            return;
        }

        DelayTarget previous = current;
        current = delayTarget;
        callback.onFrameReady(delayTarget.index);

        if (previous != null) {
            handler.obtainMessage(FrameLoaderCallback.MSG_CLEAR, previous).sendToTarget();
        }

        isLoadPending = false;
        loadNextFrame();
    }

    private class FrameLoaderCallback implements Handler.Callback {
        public static final int MSG_DELAY = 1;
        public static final int MSG_CLEAR = 2;

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_DELAY) {
                DelayTarget target = (DelayTarget) msg.obj;
                onFrameReady(target);
                return true;
            } else if (msg.what == MSG_CLEAR) {
                DelayTarget target = (DelayTarget) msg.obj;
                TxGlide.clear(target);
            }
            return false;
        }
    }

    // Visible for testing.
    static class DelayTarget extends SimpleTarget<Bitmap> {
        private final Handler handler;
        private final int index;
        private final long targetTime;
        private Bitmap resource;

        public DelayTarget(Handler handler, int index, long targetTime) {
            this.handler = handler;
            this.index = index;
            this.targetTime = targetTime;
        }

        public Bitmap getResource() {
            return resource;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            this.resource = resource;
            Message msg = handler.obtainMessage(FrameLoaderCallback.MSG_DELAY, this);
            handler.sendMessageAtTime(msg, targetTime);
        }
    }

    private static GenericRequestBuilder<GifDecoder, GifDecoder, Bitmap, Bitmap> getRequestBuilder(Context context,
                                                                                                   GifDecoder gifDecoder, int width, int height, BitmapPool bitmapPool) {
        GifFrameResourceDecoder frameResourceDecoder = new GifFrameResourceDecoder(bitmapPool);
        GifFrameModelLoader frameLoader = new GifFrameModelLoader();
        Encoder<GifDecoder> sourceEncoder = NullEncoder.get();
        return TxGlide.with(context)
                .using(frameLoader, GifDecoder.class)
                .load(gifDecoder)
                .as(Bitmap.class)
                .sourceEncoder(sourceEncoder)
                .decoder(frameResourceDecoder)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(width, height);

    }

    // Visible for testing.
    static class FrameSignature implements Key {
        private final UUID uuid;

        public FrameSignature() {
            this(UUID.randomUUID());
        }

        // VisibleForTesting.
        FrameSignature(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FrameSignature) {
                FrameSignature other = (FrameSignature) o;
                return other.uuid.equals(uuid);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
