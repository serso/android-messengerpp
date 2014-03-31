/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger.messages;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class MessagesMergeDaoResult implements MergeDaoResult<Message, String> {

	@Nonnull
	private final MergeDaoResultImpl<Message, String> mergeDaoResult = new MergeDaoResultImpl<Message, String>();

	@Nonnull
	final List<Message> readMessages = new ArrayList<Message>();

	@Override
	@Nonnull
	public List<String> getRemovedObjectIds() {
		return mergeDaoResult.getRemovedObjectIds();
	}

	@Override
	@Nonnull
	public List<Message> getAddedObjects() {
		return mergeDaoResult.getAddedObjects();
	}

	@Override
	@Nonnull
	public List<Message> getUpdatedObjects() {
		return mergeDaoResult.getUpdatedObjects();
	}

	@Nonnull
	public List<Message> getReadMessages() {
		return unmodifiableList(readMessages);
	}

	public boolean addRemovedMessageId(@Nonnull String messageId) {
		return mergeDaoResult.addRemovedObjectId(messageId);
	}

	public boolean addUpdatedMessage(@Nonnull Message message) {
		return mergeDaoResult.addUpdatedObject(message);
	}

	public boolean addAddedMessage(@Nonnull Message message) {
		return mergeDaoResult.addAddedObject(message);
	}

	public boolean addReadMessage(@Nonnull Message message) {
		return readMessages.add(message);
	}
}
