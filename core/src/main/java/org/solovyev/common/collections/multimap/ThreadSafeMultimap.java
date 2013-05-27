package org.solovyev.common.collections.multimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * User: serso
 * Date: 4/27/13
 * Time: 3:43 PM
 */
@ThreadSafe
public final class ThreadSafeMultimap<K, V> {

	@Nonnull
	public static final List<?> NO_VALUE = Collections.emptyList();

	@Nonnull
	private volatile Map<K, List<V>> map;

	private ThreadSafeMultimap(@Nonnull Map<K, List<V>> map) {
		this.map = map;
	}

	public static <K, V> ThreadSafeMultimap<K, V> newInstance() {
		return new ThreadSafeMultimap<K, V>(new HashMap<K, List<V>>());
	}


	@Nonnull
	public List<V> get(@Nonnull K key) {
		final List<V> values = map.get(key);
		if (values == null) {
			return (List<V>) NO_VALUE;
		} else {
			return Collections.unmodifiableList(values);
		}
	}

	public synchronized boolean update(@Nonnull K key, @Nonnull ListUpdater<V> updater) {
		final List<V> newValue = updater.update(get(key));
		if (newValue != null) {
			map.put(key, newValue);
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean update(@Nonnull MapUpdater<K, V> updater) {
		Map<K, List<V>> newMap = updater.update(Collections.unmodifiableMap(map));
		if (newMap != null) {
			map = newMap;
			return true;
		} else {
			return false;
		}
	}

    /*
	**********************************************************************
    *
    *                           STATIC/INNER CLASSES
    *
    **********************************************************************
    */

	public static interface ListUpdater<V> {
		@Nullable
		List<V> update(@Nonnull List<V> values);
	}

	public static interface MapUpdater<K, V> {
		@Nullable
		Map<K, List<V>> update(@Nonnull Map<K, List<V>> map);
	}

	public static <K, V> Map<K, List<V>> copy(@Nonnull Map<K, List<V>> map) {
		final Map<K, List<V>> copy = new HashMap<K, List<V>>(map.size());
		for (Map.Entry<K, List<V>> entry : map.entrySet()) {
			copy.put(entry.getKey(), copy(entry.getValue()));
		}
		return copy;
	}

	public static <V> List<V> copy(@Nonnull List<V> values) {
		return new ArrayList<V>(values);
	}
}
