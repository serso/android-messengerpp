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

package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;

import com.google.common.base.Predicate;

public class EntityByIdFinder<E extends Entity> implements Predicate<E> {

	@Nonnull
	private final String entityId;

	public EntityByIdFinder(@Nonnull String entityId) {
		this.entityId = entityId;
	}

	@Override
	public boolean apply(@Nullable Entity entity) {
		return entity != null && entityId.equals(entity.getEntityId());
	}
}
