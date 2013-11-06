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

package org.solovyev.common.collections.multimap;

import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectsChangedMapUpdater<K, V> implements ThreadSafeMultimap.MapUpdater<K, V> {

	@Nonnull
	private final Collection<V> changedObjects;

	public ObjectsChangedMapUpdater(@Nonnull V changedObject) {
		this.changedObjects = Arrays.asList(changedObject);
	}

	public ObjectsChangedMapUpdater(@Nonnull Collection<V> changedObjects) {
		this.changedObjects = changedObjects;
	}

	@Nullable
	@Override
	public Map<K, List<V>> update(@Nonnull Map<K, List<V>> map) {
		if (containsChangedObjects(map)) {
			Map<K, List<V>> result = ThreadSafeMultimap.copy(map);

			for (List<V> objects : result.values()) {
				for (int i = 0; i < objects.size(); i++) {
					final V object = objects.get(i);
					for (V changedObject : changedObjects) {
						if (object.equals(changedObject)) {
							objects.set(i, changedObject);
						}
					}
				}
			}

			return result;
		} else {
			return null;
		}

	}

	private boolean containsChangedObjects(@Nonnull Map<K, List<V>> map) {
		if (changedObjects.size() > 1) {
			return true;
		} else if (changedObjects.size() == 1) {
			final V changedObject = Iterables.getFirst(changedObjects, null);
			for (List<V> objects : map.values()) {
				if (objects.contains(changedObject)) {
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}
}
