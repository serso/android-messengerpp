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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReplacePropertyExec extends AbstractObjectDbExec<Entity> {

	@Nonnull
	private final String tableName;

	@Nonnull
	private final String idColumnName;

	@Nonnull
	private final String propertyName;

	@Nullable
	private final String propertyValue;

	public ReplacePropertyExec(@Nonnull EntityAware entityAware,
							   @Nonnull String tableName,
							   @Nonnull String idColumnName,
							   @Nonnull String propertyName,
							   @Nullable String propertyValue) {
		super(entityAware.getEntity());
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	@Override
	public long exec(@Nonnull SQLiteDatabase db) {
		final Entity entity = getNotNullObject();

		if (propertyValue != null) {
			final ContentValues values = new ContentValues();
			values.put(idColumnName, entity.getEntityId());
			values.put("property_name", propertyName);
			values.put("property_value", propertyValue);
			return db.replace(tableName, null, values);
		} else {
			return db.delete(tableName, idColumnName + " = ? and property_name = ? ", new String[]{entity.getEntityId(), propertyName});
		}
	}
}
