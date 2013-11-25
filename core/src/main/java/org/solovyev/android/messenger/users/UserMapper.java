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

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityMapper;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;
import java.util.List;

import static org.solovyev.android.messenger.users.Users.newUser;

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
		final List<AProperty> properties = dao.readPropertiesById(entity.getEntityId());

		return newUser(entity, properties);
	}
}
