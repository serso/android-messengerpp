package org.solovyev.common.collections.multimap;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

public final class ObjectAddedUpdater<V> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final V newObject;

	public ObjectAddedUpdater(@Nonnull V newObject) {
		this.newObject = newObject;
	}

	@Nullable
	@Override
	public List<V> update(@Nonnull List<V> values) {
		if (values == ThreadSafeMultimap.NO_VALUE) {
			return null;
		} else if (!Iterables.contains(values, newObject)) {
			final List<V> result = ThreadSafeMultimap.copy(values);
			result.add(newObject);
			return result;
		} else {
			return null;
		}
	}
}
