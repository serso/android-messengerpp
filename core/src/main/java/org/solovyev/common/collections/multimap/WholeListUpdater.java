package org.solovyev.common.collections.multimap;

import java.util.List;

import javax.annotation.Nonnull;

public final class WholeListUpdater<V> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final List<V> newValues;

	public WholeListUpdater(@Nonnull List<V> newValues) {
		this.newValues = newValues;
	}

	@Nonnull
	@Override
	public List<V> update(@Nonnull List<V> values) {
		return this.newValues;
	}
}
