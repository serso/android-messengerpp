/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.entities;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.collections.multimap.ThreadSafeMultimap;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class EntityAwareRemovedUpdater<V extends EntityAware> implements ThreadSafeMultimap.ListUpdater<V> {

	@Nonnull
	private final String removedEntityId;

	public EntityAwareRemovedUpdater(@Nonnull String removedEntityId) {
		this.removedEntityId = removedEntityId;
	}

	@Nullable
	@Override
	public List<V> update(@Nonnull List<V> values) {
		if (values == ThreadSafeMultimap.NO_VALUE) {
			return null;
		} else {
			final List<V> result = ThreadSafeMultimap.copy(values);
			Iterables.removeIf(result, new Predicate<V>() {
				@Override
				public boolean apply(@Nullable V entityAware) {
					return entityAware != null && entityAware.getEntity().getEntityId().equals(removedEntityId);
				}
			});
			return result;
		}
	}
}
