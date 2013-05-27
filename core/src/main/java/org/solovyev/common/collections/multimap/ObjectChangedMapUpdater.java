package org.solovyev.common.collections.multimap;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;

public class ObjectChangedMapUpdater<V> implements ThreadSafeMultimap.MapUpdater<Entity, V> {

	@Nonnull
	private final V changedObject;

	public ObjectChangedMapUpdater(@Nonnull V changedObject) {
		this.changedObject = changedObject;
	}

	@Nullable
	@Override
	public Map<Entity, List<V>> update(@Nonnull Map<Entity, List<V>> map) {
		final Map<Entity, List<V>> result = ThreadSafeMultimap.copy(map);

		for (List<V> objects : result.values()) {
			for (int i = 0; i < objects.size(); i++) {
				final V object = objects.get(i);
				if (object.equals(changedObject)) {
					objects.set(i, changedObject);
				}
			}
		}

		return result;
	}
}
