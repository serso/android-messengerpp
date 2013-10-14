package org.solovyev.common.collections.multimap;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// NOTE: we don't copy the whole map, just update VALUE in list, because it's atomic operation
public class ObjectChangedMapUpdater<K, V> implements ThreadSafeMultimap.MapUpdater<K, V> {

	@Nonnull
	private final V changedObject;

	public ObjectChangedMapUpdater(@Nonnull V changedObject) {
		this.changedObject = changedObject;
	}

	@Nullable
	@Override
	public Map<K, List<V>> update(@Nonnull Map<K, List<V>> map) {
		Map<K, List<V>> result = null;

		for (List<V> objects : map.values()) {
			for (int i = 0; i < objects.size(); i++) {
				final V object = objects.get(i);
				if (object.equals(changedObject)) {
					if(result == null) {
						result = map;
					}
					objects.set(i, changedObject);
				}
			}
		}

		return result;
	}
}
