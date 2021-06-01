package com.txt.video.widget.glide.manager;

import com.txt.video.widget.glide.RequestManager;
import com.txt.video.widget.glide.manager.RequestManagerTreeNode;

import java.util.Collections;
import java.util.Set;

/**
 * A {@link RequestManagerTreeNode} that returns no relatives.
 */
final class EmptyRequestManagerTreeNode implements RequestManagerTreeNode {
    @Override
    public Set<RequestManager> getDescendants() {
        return Collections.emptySet();
    }
}
