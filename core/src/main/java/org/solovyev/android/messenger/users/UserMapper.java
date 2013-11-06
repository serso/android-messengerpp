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
