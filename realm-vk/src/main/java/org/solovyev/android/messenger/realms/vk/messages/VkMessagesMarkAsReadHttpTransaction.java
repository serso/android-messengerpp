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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.JsonResult;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;

import javax.annotation.Nonnull;
import java.util.List;

public class VkMessagesMarkAsReadHttpTransaction extends AbstractVkHttpTransaction<Boolean> {

	@Nonnull
	private final String messageId;

	public VkMessagesMarkAsReadHttpTransaction(@Nonnull VkAccount account, @Nonnull String messageId) {
		super(account, "messages.markAsRead");
		this.messageId = messageId;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();
		result.add(new BasicNameValuePair("mids", messageId));
		return result;
	}

	@Override
	protected Boolean getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		return JsonResult.asBoolean(json);
	}

}
