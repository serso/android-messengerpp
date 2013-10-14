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
		if(containsChangedObjects(map)) {
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
