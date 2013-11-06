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

import android.database.Cursor;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 2:00 AM
 */
public class EntityMapper implements Converter<Cursor, Entity> {

	private int cursorPosition;

	private EntityMapper(int cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	@Nonnull
	public static EntityMapper newInstanceFor(int cursorPosition) {
		return new EntityMapper(cursorPosition);
	}

	@Nonnull
	@Override
	public final Entity convert(@Nonnull Cursor cursor) {
		final String entityId = cursor.getString(cursorPosition);
		final String accountId = cursor.getString(cursorPosition + 1);
		final String accountEntityId = cursor.getString(cursorPosition + 2);

		return Entities.newEntity(accountId, accountEntityId, entityId);
	}
}
