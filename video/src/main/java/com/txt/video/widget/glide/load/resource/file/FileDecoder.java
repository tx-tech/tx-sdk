package com.txt.video.widget.glide.load.resource.file;

import com.txt.video.widget.glide.load.ResourceDecoder;
import com.txt.video.widget.glide.load.engine.Resource;
import com.txt.video.widget.glide.load.resource.file.FileResource;

import java.io.File;

/**
 * A simple {@link com.txt.video.widget.glide.load.ResourceDecoder} that creates resource for a given {@link File}.
 */
public class FileDecoder implements ResourceDecoder<File, File> {

    @Override
    public Resource<File> decode(File source, int width, int height) {
        return new FileResource(source);
    }

    @Override
    public String getId() {
        return "";
    }
}
