package org.solovyev.common.collections.multimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ObjectChangedMapUpdater<K, V> implements ThreadSafeMultimap.MapUpdater<K, V> {

	@Nonnull
	private final V changedObject;

	public ObjectChangedMapUpdater(@Nonnull V changedObject) {
		this.changedObject = changedObject;
	}

	@Nullable
	@Override
	public Map<K, List<V>> update(@Nonnull Map<K, List<V>> map) {
		if(containsChangedObject(map)) {
			Map<K, List<V>> result = ThreadSafeMultimap.copy(map);

			for (List<V> objects : result.values()) {
				for (int i = 0; i < objects.size(); i++) {
					final V object = objects.get(i);
					if (object.equals(changedObject)) {
						objects.set(i, changedObject);
					}
				}
			}

			return result;
		} else {
			return null;
		}

	}

	private boolean containsChangedObject(@Nonnull Map<K, List<V>> map) {
		for (List<V> objects : map.values()) {
			if(objects.contains(changedObject)) {
				return true;
			}
		}

		return false;
	}
}
