package org.solovyev.common.collections.multimap;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public final class ObjectRemovedUpdater<V> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final V removedObject;

	public ObjectRemovedUpdater(@Nonnull V removedObject) {
		this.removedObject = removedObject;
	}

	@Nullable
	@Override
	public List<V> update(@Nonnull List<V> values) {
		if (values == ThreadSafeMultimap.NO_VALUE) {
			return null;
		} else if (Iterables.contains(values, removedObject)) {
			final List<V> result = ThreadSafeMultimap.copy(values);
			result.remove(removedObject);
			return result;
		} else {
			return null;
		}
	}
}
