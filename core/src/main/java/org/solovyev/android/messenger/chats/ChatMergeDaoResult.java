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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.messages.Message;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

final class ChatMergeDaoResult implements MergeDaoResult<Chat, String> {

	@Nonnull
	private final MergeDaoResult<Chat, String> daoResult;

	@Nonnull
	private final Multimap<Chat, Message> newMessages = ArrayListMultimap.create();

	ChatMergeDaoResult(@Nonnull MergeDaoResult<Chat, String> daoResult) {
		this.daoResult = daoResult;
	}

	@Override
	@Nonnull
	public List<String> getRemovedObjectIds() {
		return daoResult.getRemovedObjectIds();
	}

	@Override
	@Nonnull
	public List<Chat> getAddedObjects() {
		return daoResult.getAddedObjects();
	}

	@Override
	@Nonnull
	public List<Chat> getUpdatedObjects() {
		return daoResult.getUpdatedObjects();
	}

	public void addNewMessages(@Nonnull Chat chat, @Nonnull List<Message> messages) {
		if (!messages.isEmpty()) {
			newMessages.putAll(chat, messages);
		}
	}

	@Nonnull
	public Map<Chat, Collection<Message>> getNewMessages() {
		return newMessages.asMap();
	}
}
