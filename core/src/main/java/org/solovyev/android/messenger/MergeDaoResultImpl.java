package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* User: serso
* Date: 6/9/12
* Time: 8:19 PM
*/
public class MergeDaoResultImpl<T, ID> implements MergeDaoResult<T, ID> {

    // ids of friends which were removed on the remote server
    @NotNull
    private final List<ID> removedObjectIds;

    // friends which were added on the remote server
    @NotNull
    private final List<T> addedObjectLinks;

    @NotNull
    private final List<T> addedObjects;

    // nothing changed => just update friends' properties
    @NotNull
    private final List<T> updatedObjects;

    @NotNull
    private final List<? extends T> objects;

    public MergeDaoResultImpl(@NotNull List<? extends T> objects) {
        this.objects = objects;
        removedObjectIds = new ArrayList<ID>(objects.size());
        addedObjectLinks = new ArrayList<T>(objects.size());
        addedObjects = new ArrayList<T>(objects.size());
        updatedObjects = new ArrayList<T>(objects.size());
    }

    @NotNull
    public List<ID> getRemovedObjectIds() {
        return Collections.unmodifiableList(removedObjectIds);
    }

    @NotNull
    public List<T> getAddedObjectLinks() {
        return Collections.unmodifiableList(addedObjectLinks);
    }

    @NotNull
    public List<T> getAddedObjects() {
        return Collections.unmodifiableList(addedObjects);
    }

    @NotNull
    public List<T> getUpdatedObjects() {
        return Collections.unmodifiableList(updatedObjects);
    }

    public boolean addRemovedObjectId(@NotNull ID removedObjectId) {
        return removedObjectIds.add(removedObjectId);
    }

    public boolean addAddedObjectLink(@NotNull T addedLink) {
        return addedObjectLinks.add(addedLink);
    }

    public boolean addAddedObject(@NotNull T addedObject) {
        return addedObjects.add(addedObject);
    }

    public boolean addUpdatedObject(@NotNull T updatedObject) {
        return updatedObjects.add(updatedObject);
    }
}
