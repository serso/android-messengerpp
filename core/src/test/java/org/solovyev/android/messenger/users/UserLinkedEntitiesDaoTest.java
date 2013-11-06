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

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.DefaultLinkedEntitiesDaoTest;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.chats.ChatDao;

import com.google.common.base.Function;
import com.google.inject.Inject;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class UserLinkedEntitiesDaoTest extends DefaultLinkedEntitiesDaoTest<User> {

	@Inject
	@Nonnull
	private UserDao dao;

	@Inject
	@Nonnull
	private ChatDao chatDao;

	public UserLinkedEntitiesDaoTest() {
		super(new UserSameEqualizer());
	}

	@Nonnull
	@Override
	protected LinkedEntitiesDao<User> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected List<User> getLinkedEntities(@Nonnull AccountData ad) {
		return ad.getContacts();
	}

	@Nonnull
	@Override
	protected User newLinkedEntity(@Nonnull AccountData ad, int i) {
		return newEmptyUser(ad.getAccount().newUserEntity("linked_contact_" + i));
	}

	@Nonnull
	@Override
	protected String getId() {
		return getAccount1().getUser().getId();
	}

	@Nonnull
	@Override
	protected Collection<String> getLinkedIds() {
		return newArrayList(transform(getAccountData1().getContacts(), new Function<User, String>() {
			@Override
			public String apply(@Nullable User contact) {
				return contact.getId();
			}
		}));
	}
}
