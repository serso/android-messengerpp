package org.solovyev.common.collections.multimap;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public final class ObjectsAddedUpdater<V> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final Collection<V> newObjects;

	public ObjectsAddedUpdater(@Nonnull Collection<V> newObjects) {
		this.newObjects = newObjects;
	}

	@Nullable
	@Override
	public List<V> update(@Nonnull List<V> values) {
		if (values == ThreadSafeMultimap.NO_VALUE) {
			return null;
		} else {
			List<V> result = null;
			for (V newObject : newObjects) {
				if (!Iterables.contains(values, newObject)) {
					if (result == null) {
						result = ThreadSafeMultimap.copy(values);
					}
					result.add(newObject);
				}
			}
			return result;
		}
	}
}
