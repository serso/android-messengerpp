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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.vk.VkAccount;
import org.solovyev.android.messenger.realms.vk.http.AbstractVkHttpTransaction;

import javax.annotation.Nonnull;
import java.util.List;

public class VkFriendsGetOnlineHttpTransaction extends AbstractVkHttpTransaction<List<String>> {

	@Nonnull
	private final String userId;

	public VkFriendsGetOnlineHttpTransaction(@Nonnull VkAccount account, @Nonnull String userId) {
		super(account, "friends.getOnline");
		this.userId = userId;
	}

	public VkFriendsGetOnlineHttpTransaction(@Nonnull VkAccount account) {
		super(account, "friends.getOnline");
		this.userId = account.getUser().getEntity().getAccountEntityId();
	}


	@Override
	protected List<String> getResponseFromJson(@Nonnull String json) throws IllegalJsonException {
		try {
			return JsonUserIds.newFromJson(json).getIds();
		} catch (IllegalJsonRuntimeException e) {
			throw e.getIllegalJsonException();
		}
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = super.getRequestParameters();

		result.add(new BasicNameValuePair("uid", userId));

		return result;
	}
}
