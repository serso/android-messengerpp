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

package org.solovyev.common.collections.multimap;

import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
