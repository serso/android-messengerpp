/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
