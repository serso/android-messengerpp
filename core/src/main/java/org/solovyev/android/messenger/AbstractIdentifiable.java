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

import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.MutableEntity;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 2:26 PM
 */
public abstract class AbstractIdentifiable extends JObject implements Identifiable {

	@Nonnull
	private /*final*/ MutableEntity entity;

	protected AbstractIdentifiable(@Nonnull Entity entity) {
		if(entity instanceof MutableEntity) {
			this.entity = (MutableEntity) entity;
		} else {
			this.entity = Entities.newEntity(entity.getAccountId(), entity.getAccountEntityId(), entity.getEntityId());
		}
	}

	@Nonnull
	@Override
	public final String getId() {
		return entity.getEntityId();
	}

	@Nonnull
	public MutableEntity getEntity() {
		return entity;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}

		final AbstractIdentifiable that = (AbstractIdentifiable) o;

		if (!entity.equals(that.entity)) {
			return false;
		}

		return true;
	}

	@Override
	public final int hashCode() {
		return entity.hashCode();
	}

	@Nonnull
	@Override
	public AbstractIdentifiable clone() {
		final AbstractIdentifiable clone = (AbstractIdentifiable) super.clone();

		clone.entity = entity.clone();

		return clone;
	}

	@Nonnull
	protected AbstractIdentifiable cloneWithNewEntity0(@Nonnull MutableEntity entity) {
		final AbstractIdentifiable clone = (AbstractIdentifiable) super.clone();

		clone.entity = entity;

		return clone;
	}
}
