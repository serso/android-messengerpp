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

package org.solovyev.android.messenger.realms.vk.chats;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VkMessagesGetHttpTransaction extends AbstractVkHttpTransaction<List<Message>> {

	@Nullable
	private Integer count;

	protected VkMessagesGetHttpTransaction(@Nonnull VkAccount account) {
		super(account, "messages.get");
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> requestParameters = super.getRequestParameters();

		if (count != null) {
			requestParameters.add(new BasicNameValuePair("count", String.valueOf(count)));
		}

		return requestParameters;
	}

	@Override
	protected List<Message> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		final List<AccountChat> chats = new JsonChatConverter(getAccount().getUser(), null, null, App.getUserService(), getAccount()).convert(json);

		// todo serso: optimize - convert json to the messages directly
		final List<Message> messages = new ArrayList<Message>(chats.size() * 10);
		for (AccountChat chat : chats) {
			messages.addAll(chat.getMessages());
		}

		return messages;
	}
}
