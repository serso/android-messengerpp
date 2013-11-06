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

package org.solovyev.android.messenger.chats;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.DefaultLinkedEntitiesDaoTest;
import org.solovyev.android.messenger.LinkedEntitiesDao;

import com.google.common.base.Function;
import com.google.inject.Inject;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

public class ChatLinkedEntitiesDaoTest extends DefaultLinkedEntitiesDaoTest<Chat> {

	@Inject
	@Nonnull
	private ChatDao dao;

	@Nonnull
	@Override
	protected LinkedEntitiesDao<Chat> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected Chat newLinkedEntity(@Nonnull AccountData ad, int i) {
		return Chats.newPrivateChat(ad.getAccount().newChatEntity("linked_chat_" + i));
	}

	@Nonnull
	@Override
	protected List<Chat> getLinkedEntities(@Nonnull AccountData ad) {
		return newArrayList(transform(ad.getChats(), new Function<AccountChat, Chat>() {
			@Override
			public Chat apply(@Nullable AccountChat accountChat) {
				return accountChat.getChat();
			}
		}));
	}

	@Nonnull
	@Override
	protected String getId() {
		return getAccountData1().getAccount().getUser().getId();
	}

	@Nonnull
	@Override
	protected Collection<String> getLinkedIds() {
		return newArrayList(transform(getAccountData1().getChats(), new Function<AccountChat, String>() {
			@Override
			public String apply(@Nullable AccountChat accountChat) {
				return accountChat.getChat().getId();
			}
		}));
	}
}
