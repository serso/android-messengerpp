package org.solovyev.android.messenger.users;

import android.database.Cursor;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

import static org.solovyev.android.messenger.users.Users.newUser;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 7:03 PM
 */
public class UserMapper implements Converter<Cursor, User> {

	@Nonnull
	private final UserDao dao;

	public UserMapper(@Nonnull UserDao dao) {
		this.dao = dao;
	}

	@Nonnull
	@Override
	public User convert(@Nonnull Cursor c) {
		final Entity entity = EntityMapper.newInstanceFor(0).convert(c);

		final UserSyncData userSyncData = Users.newUserSyncData(c.getString(3), c.getString(4), c.getString(5), c.getString(6));

		final List<AProperty> properties = dao.readPropertiesById(entity.getEntityId());

		return newUser(entity, userSyncData, properties);
	}
}
