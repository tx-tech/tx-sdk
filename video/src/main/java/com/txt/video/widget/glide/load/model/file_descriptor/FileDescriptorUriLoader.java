package com.txt.video.widget.glide.load.model.file_descriptor;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.txt.video.widget.glide.TxGlide;
import com.txt.video.widget.glide.load.data.DataFetcher;
import com.txt.video.widget.glide.load.data.FileDescriptorAssetPathFetcher;
import com.txt.video.widget.glide.load.data.FileDescriptorLocalUriFetcher;
import com.txt.video.widget.glide.load.model.GenericLoaderFactory;
import com.txt.video.widget.glide.load.model.GlideUrl;
import com.txt.video.widget.glide.load.model.ModelLoader;
import com.txt.video.widget.glide.load.model.ModelLoaderFactory;
import com.txt.video.widget.glide.load.model.UriLoader;

/**
 * A {@link ModelLoader} For translating {@link Uri} models for local uris into {@link ParcelFileDescriptor} data.
 */
public class FileDescriptorUriLoader extends UriLoader<ParcelFileDescriptor> implements FileDescriptorModelLoader<Uri> {

    /**
     * The default factory for {@link FileDescriptorUriLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<Uri, ParcelFileDescriptor> {
        @Override
        public ModelLoader<Uri, ParcelFileDescriptor> build(Context context, GenericLoaderFactory factories) {
            return new FileDescriptorUriLoader(context, factories.buildModelLoader(GlideUrl.class,
                    ParcelFileDescriptor.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public FileDescriptorUriLoader(Context context) {
        this(context, TxGlide.buildFileDescriptorModelLoader(GlideUrl.class, context));
    }

    public FileDescriptorUriLoader(Context context, ModelLoader<GlideUrl, ParcelFileDescriptor> urlLoader) {
        super(context, urlLoader);
    }

    @Override
    protected DataFetcher<ParcelFileDescriptor> getLocalUriFetcher(Context context, Uri uri) {
        return new FileDescriptorLocalUriFetcher(context, uri);
    }

    @Override
    protected DataFetcher<ParcelFileDescriptor> getAssetPathFetcher(Context context, String assetPath) {
        return new FileDescriptorAssetPathFetcher(context.getApplicationContext().getAssets(), assetPath);
    }
}
