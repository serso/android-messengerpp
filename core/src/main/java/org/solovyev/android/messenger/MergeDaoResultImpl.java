package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* User: serso
* Date: 6/9/12
* Time: 8:19 PM
*/
public class MergeDaoResultImpl<T, ID> implements MergeDaoResult<T, ID> {

    // ids of objects which were removed on the remote server
    @Nonnull
    private final List<ID> removedObjectIds;

    // objects which were added on the remote server
    @Nonnull
    private final List<T> addedObjectLinks;

    @Nonnull
    private final List<T> addedObjects;

    // nothing changed => just update objects' properties
    @Nonnull
    private final List<T> updatedObjects;

    @Nonnull
    private final List<? extends T> objects;

    public MergeDaoResultImpl(@Nonnull List<? extends T> objects) {
        this.objects = objects;
        removedObjectIds = new ArrayList<ID>(objects.size());
        addedObjectLinks = new ArrayList<T>(objects.size());
        addedObjects = new ArrayList<T>(objects.size());
        updatedObjects = new ArrayList<T>(objects.size());
    }

    @Nonnull
    public List<ID> getRemovedObjectIds() {
        return Collections.unmodifiableList(removedObjectIds);
    }

    @Nonnull
    public List<T> getAddedObjectLinks() {
        return Collections.unmodifiableList(addedObjectLinks);
    }

    @Nonnull
    public List<T> getAddedObjects() {
        return Collections.unmodifiableList(addedObjects);
    }

    @Nonnull
    public List<T> getUpdatedObjects() {
        return Collections.unmodifiableList(updatedObjects);
    }

    public boolean addRemovedObjectId(@Nonnull ID removedObjectId) {
        return removedObjectIds.add(removedObjectId);
    }

    public boolean addAddedObjectLink(@Nonnull T addedLink) {
        return addedObjectLinks.add(addedLink);
    }

    public boolean addAddedObject(@Nonnull T addedObject) {
        return addedObjects.add(addedObject);
    }

    public boolean addUpdatedObject(@Nonnull T updatedObject) {
        return updatedObjects.add(updatedObject);
    }
}
