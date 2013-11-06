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

package org.solovyev.android.messenger.realms.xmpp;

import com.google.gson.Gson;
import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class XmppAccountConfiguration extends JObject implements AccountConfiguration {

	private static final boolean DEBUG = true;

	private static final int DEFAULT_PORT = 5222;

	@Nonnull
	private static final String DEFAULT_RESOURCE = "Messenger++";

	@Nonnull
	private String server;

	@Nonnull
	private String login;

	@Nonnull
	private String password;

	@Nonnull
	private String resource = DEFAULT_RESOURCE;

	@Nonnull
	private Integer port = DEFAULT_PORT;

	// for gson
	public XmppAccountConfiguration() {
	}

	public XmppAccountConfiguration(@Nonnull String server, @Nonnull String login, @Nonnull String password) {
		this.server = server;
		this.login = login;
		this.password = password;
	}

	@Nonnull
	public String getAccountUserId() {
		final int index = login.indexOf('@');
		if (index < 0) {
			return login + '@' + server;
		} else {
			return login;
		}
	}

	@Nonnull
	public String getServer() {
		return server;
	}

	@Nonnull
	public String getLogin() {
		return login;
	}

	@Nonnull
	public String getPassword() {
		return password;
	}

	void setResource(@Nonnull String resource) {
		this.resource = resource;
	}

	void setPort(@Nonnull Integer port) {
		this.port = port;
	}

	@Nonnull
	public String getResource() {
		return resource;
	}

	@Nonnull
	public Integer getPort() {
		return port;
	}

	@Nonnull
	public AndroidConnectionConfiguration toXmppConfiguration() {
		final AndroidConnectionConfiguration connectionConfiguration = new AndroidConnectionConfiguration(this.server, this.port, null);

		connectionConfiguration.setDebuggerEnabled(DEBUG);

		return connectionConfiguration;
	}

	@Override
	public boolean isSameAccount(AccountConfiguration c) {
		if (!(c instanceof XmppAccountConfiguration)) return false;

		XmppAccountConfiguration that = (XmppAccountConfiguration) c;

		if (!login.equals(that.login)) return false;
		if (!port.equals(that.port)) return false;
		if (!server.equals(that.server)) return false;

		return true;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		boolean same = isSameAccount(c);
		if (same) {
			final XmppAccountConfiguration that = (XmppAccountConfiguration) c;
			same = this.password.equals(that.password);
		}
		return same;
	}

	@Override
	public boolean isSame(AccountConfiguration c) {
		boolean same = isSameCredentials(c);
		if (same) {
			final XmppAccountConfiguration that = (XmppAccountConfiguration) c;
			same = this.resource.equals(that.resource);
		}
		return same;
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
	}

	@Nonnull
	public static XmppAccountConfiguration fromJson(@Nonnull String json) {
		return new Gson().fromJson(json, XmppAccountConfiguration.class);
	}

	@Nonnull
	@Override
	public XmppAccountConfiguration clone() {
		return (XmppAccountConfiguration) super.clone();
	}

	public void setPassword(@Nonnull String password) {
		this.password = password;
	}
}
