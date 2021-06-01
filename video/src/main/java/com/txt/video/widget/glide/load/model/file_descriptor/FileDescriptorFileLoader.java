package com.txt.video.widget.glide.load.model.file_descriptor;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.txt.video.widget.glide.TxGlide;
import com.txt.video.widget.glide.load.model.FileLoader;
import com.txt.video.widget.glide.load.model.GenericLoaderFactory;
import com.txt.video.widget.glide.load.model.ModelLoader;
import com.txt.video.widget.glide.load.model.ModelLoaderFactory;

import java.io.File;

/**
 * A {@link ModelLoader} For translating {@link File} models into {@link ParcelFileDescriptor} data.
 */
public class FileDescriptorFileLoader extends FileLoader<ParcelFileDescriptor>
        implements FileDescriptorModelLoader<File> {

    /**
     * The default {@link com.txt.video.widget.glide.load.model.ModelLoaderFactory} for
     * {@link FileDescriptorFileLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<File, ParcelFileDescriptor> {
        @Override
        public ModelLoader<File, ParcelFileDescriptor> build(Context context, GenericLoaderFactory factories) {
            return new FileDescriptorFileLoader(factories.buildModelLoader(Uri.class, ParcelFileDescriptor.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public FileDescriptorFileLoader(Context context) {
        this(TxGlide.buildFileDescriptorModelLoader(Uri.class, context));
    }

    public FileDescriptorFileLoader(ModelLoader<Uri, ParcelFileDescriptor> uriLoader) {
        super(uriLoader);
    }
}
