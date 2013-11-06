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
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:03 PM
 */
public class VkMessagesGetDialogsHttpTransaction extends AbstractVkHttpTransaction<List<AccountChat>> {

	@Nonnull
	private static final Integer MAX_COUNT = 100;

	@Nonnull
	private final Integer count;

	@Nonnull
	private final User user;

	private VkMessagesGetDialogsHttpTransaction(@Nonnull VkAccount realm, @Nonnull Integer count, @Nonnull User user) {
		super(realm, "messages.getDialogs");
		this.count = count;
		this.user = user;
	}

	@Nonnull
	public static VkMessagesGetDialogsHttpTransaction newInstance(@Nonnull VkAccount realm, @Nonnull User user) {
		return new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user);
	}

	@Nonnull
	public static List<VkMessagesGetDialogsHttpTransaction> newInstances(@Nonnull VkAccount realm, @Nonnull Integer count, @Nonnull User user) {
		final List<VkMessagesGetDialogsHttpTransaction> result = new ArrayList<VkMessagesGetDialogsHttpTransaction>();

		for (int i = 0; i < count / MAX_COUNT; i++) {
			result.add(new VkMessagesGetDialogsHttpTransaction(realm, MAX_COUNT, user));
		}

		if (count % MAX_COUNT != 0) {
			result.add(new VkMessagesGetDialogsHttpTransaction(realm, count % MAX_COUNT, user));
		}

		return result;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();

		result.add(new BasicNameValuePair("count", String.valueOf(count)));

		return result;
	}

	@Override
	protected List<AccountChat> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		return new JsonChatConverter(user, null, null, App.getUserService(), getAccount()).convert(json);
	}
}
