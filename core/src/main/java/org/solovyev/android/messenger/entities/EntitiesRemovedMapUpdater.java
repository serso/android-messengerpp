package org.solovyev.android.messenger.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.solovyev.common.collections.multimap.ThreadSafeMultimap;

public final class EntitiesRemovedMapUpdater<V> implements ThreadSafeMultimap.MapUpdater<Entity, V> {

	@Nonnull
	private final String realmId;

	private EntitiesRemovedMapUpdater(@Nonnull String realmId) {
		this.realmId = realmId;
	}

	@Nonnull
	public static <V> ThreadSafeMultimap.MapUpdater<Entity, V> newInstance(@Nonnull String realmId) {
		return new EntitiesRemovedMapUpdater<V>(realmId);
	}

	@Nonnull
	@Override
	public Map<Entity, List<V>> update(@Nonnull Map<Entity, List<V>> map) {
		final Map<Entity, List<V>> result = new HashMap<Entity, List<V>>(map.size());

		for (Map.Entry<Entity, List<V>> entry : map.entrySet()) {
			if (!entry.getKey().getRealmId().equals(realmId)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}

		return result;
	}
}
