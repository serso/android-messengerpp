package org.solovyev.android.messenger;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static java.util.Collections.unmodifiableList;

public final class MergeDaoResultImpl<T, ID> implements MergeDaoResult<T, ID> {

	// ids of objects which were removed on the remote server
	@Nonnull
	private final List<ID> removedObjectIds;

	@Nonnull
	private final List<T> addedObjects;

	// nothing changed => just update objects' properties
	@Nonnull
	private final List<T> updatedObjects;

	public MergeDaoResultImpl() {
		this.removedObjectIds = new ArrayList<ID>();
		this.addedObjects = new ArrayList<T>();
		this.updatedObjects = new ArrayList<T>();
	}

	@Nonnull
	public List<ID> getRemovedObjectIds() {
		return unmodifiableList(removedObjectIds);
	}

	@Nonnull
	public List<T> getAddedObjects() {
		return unmodifiableList(addedObjects);
	}

	@Nonnull
	public List<T> getUpdatedObjects() {
		return unmodifiableList(updatedObjects);
	}

	public boolean addRemovedObjectId(@Nonnull ID removedObjectId) {
		return removedObjectIds.add(removedObjectId);
	}

	public boolean addAddedObject(@Nonnull T addedObject) {
		return addedObjects.add(addedObject);
	}

	public boolean addUpdatedObject(@Nonnull T updatedObject) {
		return updatedObjects.add(updatedObject);
	}
}
