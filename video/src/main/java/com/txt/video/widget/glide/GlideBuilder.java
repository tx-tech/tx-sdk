package com.txt.video.widget.glide;

import android.content.Context;
import android.os.Build;

import com.txt.video.widget.glide.load.DecodeFormat;
import com.txt.video.widget.glide.load.engine.Engine;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPool;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.txt.video.widget.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.txt.video.widget.glide.load.engine.cache.DiskCache;
import com.txt.video.widget.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.txt.video.widget.glide.load.engine.cache.LruResourceCache;
import com.txt.video.widget.glide.load.engine.cache.MemoryCache;
import com.txt.video.widget.glide.load.engine.cache.MemorySizeCalculator;
import com.txt.video.widget.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;

import java.util.concurrent.ExecutorService;

/**
 * A builder class for setting default structural classes for Glide to use.
 */
public class GlideBuilder {
    private final Context context;

    private Engine engine;
    private BitmapPool bitmapPool;
    private MemoryCache memoryCache;
    private ExecutorService sourceService;
    private ExecutorService diskCacheService;
    private DecodeFormat decodeFormat;
    private DiskCache.Factory diskCacheFactory;

    public GlideBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Sets the {@link BitmapPool} implementation to use to store and
     * retrieve reused {@link android.graphics.Bitmap}s.
     *
     * @param bitmapPool The pool to use.
     * @return This builder.
     */
    public GlideBuilder setBitmapPool(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
        return this;
    }

    /**
     * Sets the {@link MemoryCache} implementation to store
     * {@link com.txt.video.widget.glide.load.engine.Resource}s that are not currently in use.
     *
     * @param memoryCache  The cache to use.
     * @return This builder.
     */
    public GlideBuilder setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    /**
     * Sets the {@link DiskCache} implementation to use to store
     * {@link com.txt.video.widget.glide.load.engine.Resource} data and thumbnails.
     *
     * @deprecated Creating a disk cache directory on the main thread causes strict mode violations, use
     * {@link #setDiskCache(DiskCache.Factory)} instead. Scheduled to be removed
     * in Glide 4.0.
     * @param diskCache The disk cache to use.
     * @return This builder.
     */
    @Deprecated
    public GlideBuilder setDiskCache(final DiskCache diskCache) {
        return setDiskCache(new DiskCache.Factory() {
            @Override
            public DiskCache build() {
                return diskCache;
            }
        });
    }

    /**
     * Sets the {@link DiskCache.Factory} implementation to use to construct
     * the {@link DiskCache} to use to store
     * {@link com.txt.video.widget.glide.load.engine.Resource} data on disk.
     *
     * @param diskCacheFactory The disk cche factory to use.
     * @return This builder.
     */
    public GlideBuilder setDiskCache(DiskCache.Factory diskCacheFactory) {
        this.diskCacheFactory = diskCacheFactory;
        return this;
    }

    /**
     * Sets the {@link ExecutorService} implementation to use when retrieving
     * {@link com.txt.video.widget.glide.load.engine.Resource}s that are not already in the cache.
     *
     * <p>
     *     Any implementation must order requests based on their {@link Priority} for thumbnail
     *     requests to work properly.
     * </p>
     *
     * @see #setDiskCacheService(ExecutorService)
     * @see FifoPriorityThreadPoolExecutor
     *
     * @param service The ExecutorService to use.
     * @return This builder.
     */
    public GlideBuilder setResizeService(ExecutorService service) {
        this.sourceService = service;
        return this;
    }

    /**
     * Sets the {@link ExecutorService} implementation to use when retrieving
     * {@link com.txt.video.widget.glide.load.engine.Resource}s that are currently in cache.
     *
     * <p>
     *     Any implementation must order requests based on their {@link Priority} for thumbnail
     *     requests to work properly.
     * </p>
     *
     * @see #setResizeService(ExecutorService)
     * @see FifoPriorityThreadPoolExecutor
     *
     * @param service The ExecutorService to use.
     * @return This builder.
     */
    public GlideBuilder setDiskCacheService(ExecutorService service) {
        this.diskCacheService = service;
        return this;
    }

    /**
     * Sets the {@link DecodeFormat} that will be the default format for all the default
     * decoders that can change the {@link android.graphics.Bitmap.Config} of the {@link android.graphics.Bitmap}s they
     * decode.
     *
     * <p>
     *     Decode format is always a suggestion, not a requirement. See {@link DecodeFormat} for
     *     more details.
     * </p>
     *
     * <p>
     *     If you instantiate and use a custom decoder, it will use
     *     {@link DecodeFormat#DEFAULT} as its default.
     * </p>
     *
     * <p>
     *     Calls to this method are ignored on KitKat and Lollipop. See #301.
     * </p>
     *
     * @param decodeFormat The format to use.
     * @return This builder.
     */
    public GlideBuilder setDecodeFormat(DecodeFormat decodeFormat) {
        this.decodeFormat = decodeFormat;
        return this;
    }

    // For testing.
    GlideBuilder setEngine(Engine engine) {
        this.engine = engine;
        return this;
    }

    TxGlide createGlide() {
        if (sourceService == null) {
            final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            sourceService = new FifoPriorityThreadPoolExecutor(cores);
        }
        if (diskCacheService == null) {
            diskCacheService = new FifoPriorityThreadPoolExecutor(1);
        }

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        if (bitmapPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int size = calculator.getBitmapPoolSize();
                bitmapPool = new LruBitmapPool(size);
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());
        }

        if (diskCacheFactory == null) {
            diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }

        if (engine == null) {
            engine = new Engine(memoryCache, diskCacheFactory, diskCacheService, sourceService);
        }

        if (decodeFormat == null) {
            decodeFormat = DecodeFormat.DEFAULT;
        }

        return new TxGlide(engine, memoryCache, bitmapPool, context, decodeFormat);
    }
}