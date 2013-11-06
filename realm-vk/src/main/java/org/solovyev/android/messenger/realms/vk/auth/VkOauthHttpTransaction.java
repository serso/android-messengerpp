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

package org.solovyev.android.messenger.realms.vk.auth;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.http.VkResponseErrorException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VkOauthHttpTransaction extends AbstractHttpTransaction<JsonAuthResult> {

	@Nonnull
	private final String login;

	@Nonnull
	private final String password;

	public VkOauthHttpTransaction(@Nonnull String login, @Nonnull String password) {
		super("https://api.vk.com/oauth/token", HttpMethod.GET);
		this.login = login;
		this.password = password;
	}

	@Override
	public JsonAuthResult getResponse(@Nonnull HttpResponse response) {
		boolean ok = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		if (!ok) {
			throw new HttpRuntimeIoException(new IOException());
		}

		final String json;
		try {
			json = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			throw new HttpRuntimeIoException(e);
		}

		try {
			return JsonAuthResult.fromJson(json);
		} catch (IllegalJsonException e) {
			throw VkResponseErrorException.newInstance(json, this);
		}
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("grant_type", "password"));
		values.add(new BasicNameValuePair("client_id", "2965041"));
		values.add(new BasicNameValuePair("client_secret", "fXK28HAI0nVRK3hNZiGs"));
		//values.add(new BasicNameValuePair("client_id", "2970921"));
		//values.add(new BasicNameValuePair("client_secret", "Scm7M1vxOdDjpeVj81jw"));
		values.add(new BasicNameValuePair("username", login));
		values.add(new BasicNameValuePair("password", password));
		values.add(new BasicNameValuePair("scope", "friends,messages,notifications,offline,photos,audio,video,docs,notify,notes,status,groups,wall"));
		return values;
	}
}
