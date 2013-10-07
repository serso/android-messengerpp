package org.solovyev.android.messenger;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

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

	@Nonnull
	private final String propertyValue;

	public ReplacePropertyExec(@Nonnull EntityAware entityAware,
							   @Nonnull String tableName,
							   @Nonnull String idColumnName,
							   @Nonnull String propertyName,
							   @Nonnull String propertyValue) {
		super(entityAware.getEntity());
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	@Override
	public long exec(@Nonnull SQLiteDatabase db) {
		final ContentValues values = new ContentValues();
		final Entity entity = getNotNullObject();
		values.put(idColumnName, entity.getEntityId());
		values.put("property_name", propertyName);
		values.put("property_value", propertyValue);
		return db.replace(tableName, null, values);
	}
}
