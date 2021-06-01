package com.txt.video.widget.glide.load.resource.file;

import com.txt.video.widget.glide.load.resource.SimpleResource;

import java.io.File;

/**
 * A simple {@link com.txt.video.widget.glide.load.engine.Resource} that wraps a {@link File}.
 */
public class FileResource extends SimpleResource<File> {
    public FileResource(File file) {
        super(file);
    }
}
