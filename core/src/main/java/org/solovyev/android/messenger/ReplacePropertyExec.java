package org.solovyev.android.messenger;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;

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
