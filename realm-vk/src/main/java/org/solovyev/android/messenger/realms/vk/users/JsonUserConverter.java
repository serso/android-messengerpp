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

package org.solovyev.android.messenger.realms.vk.users;

import com.google.common.base.Function;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.Converter;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.realms.vk.users.JsonUsers.newFromJson;

public class JsonUserConverter implements Converter<String, List<User>> {

	@Nonnull
	private final Account account;

	private JsonUserConverter(@Nonnull Account account) {
		this.account = account;
	}

	@Nonnull
	@Override
	public List<User> convert(@Nonnull String json) {
		final JsonUsers jsonUsersResult = newFromJson(json);
		final List<JsonUser> jsonUsers = jsonUsersResult.getUsers();
		return newArrayList(transform(jsonUsers, new Function<JsonUser, User>() {
			@Override
			public User apply(JsonUser jsonUser) {
				try {
					return jsonUser.toUser(account);
				} catch (IllegalJsonException e) {
					throw new IllegalJsonRuntimeException(e);
				}
			}
		}));
	}

	@Nonnull
	public static Converter<String, List<User>> newInstance(@Nonnull Account account) {
		return new JsonUserConverter(account);
	}
}
