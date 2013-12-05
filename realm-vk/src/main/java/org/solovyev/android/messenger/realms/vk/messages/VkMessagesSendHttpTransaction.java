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

package org.solovyev.android.messenger.realms.vk.messages;

import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class VkMessagesSendHttpTransaction extends AbstractVkHttpTransaction<String> {

	@Nonnull
	private final Message message;

	@Nonnull
	private final Chat chat;

	public VkMessagesSendHttpTransaction(@Nonnull VkAccount account, @Nonnull Message message, @Nonnull Chat chat) {
		super(account, "messages.send");
		this.message = message;
		this.chat = chat;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();

		if (chat.isPrivate()) {
			result.add(new BasicNameValuePair("uid", chat.getSecondUser().getAccountEntityId()));
		}

		if (!chat.isPrivate()) {
			result.add(new BasicNameValuePair("chat_id", chat.getEntity().getAccountEntityId()));
		}

		result.add(new BasicNameValuePair("message", message.getBody()));
		result.add(new BasicNameValuePair("title", message.getTitle()));
		result.add(new BasicNameValuePair("type", message.isPrivate() ? "0" : "1"));

		return result;
	}

	@Override
	protected String getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		return new Gson().fromJson(json, JsonResult.class).response;
	}

	public static class JsonResult {

		@Nullable
		private String response;
	}
}
