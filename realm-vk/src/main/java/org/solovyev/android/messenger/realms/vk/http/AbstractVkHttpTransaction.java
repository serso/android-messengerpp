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

package org.solovyev.android.messenger.realms.vk.http;

import android.util.Log;
import com.google.gson.JsonParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.VkAccount;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVkHttpTransaction<R> extends AbstractHttpTransaction<R> {

	private static final String URI = "https://api.vk.com/method/";

	@Nonnull
	private final VkAccount account;

	protected AbstractVkHttpTransaction(@Nonnull VkAccount account, @Nonnull String method) {
		this(account, method, HttpMethod.GET);
	}

	protected AbstractVkHttpTransaction(@Nonnull VkAccount account, @Nonnull String method, @Nonnull HttpMethod httpMethod) {
		super(URI + method, httpMethod);
		this.account = account;
	}

	@Nonnull
	protected VkAccount getAccount() {
		return account;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();
		result.add(new BasicNameValuePair("access_token", getAccount().getConfiguration().getAccessToken()));
		return result;
	}

	@Override
	public R getResponse(@Nonnull HttpResponse response) {
		try {
			final HttpEntity httpEntity = response.getEntity();
			final String entity = EntityUtils.toString(httpEntity);

			checkStatusCode(response, entity);

			Log.d(getClass().getSimpleName(), "Json: " + entity);

			try {
				return getResponseFromJson(entity);
			} catch (JsonParseException e) {
				throw new AccountRuntimeException(account.getId(), VkResponseErrorException.newInstance(entity, this));
			} catch (IllegalJsonException e) {
				throw new AccountRuntimeException(account.getId(), VkResponseErrorException.newInstance(entity, this));
			}
		} catch (IOException e) {
			throw new HttpRuntimeIoException(e);
		}
	}

	private void checkStatusCode(@Nonnull HttpResponse response, @Nullable String entity) {
		final int statusCode = response.getStatusLine().getStatusCode();
		final boolean ok = statusCode == HttpStatus.SC_OK;
		if (!ok) {
			Log.e(getClass().getSimpleName(), "Error in HTTP request: " + createRequest().getURI());
			Log.e(getClass().getSimpleName(), "Got response: " + entity);
			throw new HttpRuntimeIoException(new IOException("Error status code: " + statusCode));
		}
	}

	protected abstract R getResponseFromJson(@Nonnull String json) throws IllegalJsonException;
}
