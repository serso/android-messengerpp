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
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppAccountConfiguration extends JObject implements AccountConfiguration {

	private static final boolean DEBUG = true;

	static final int DEFAULT_PORT = 5222;

	static final XmppSecurityMode DEFAULT_SECURITY_MODE = XmppSecurityMode.enabled;

	@Nonnull
	static final String DEFAULT_RESOURCE = "Messenger++";

	static final char AT = '@';

	static final boolean DEFAULT_USE_LOGIN_WITH_DOMAIN = true;

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

	@Nonnull
	private XmppSecurityMode securityMode = DEFAULT_SECURITY_MODE;

	private boolean useLoginWithDomain = DEFAULT_USE_LOGIN_WITH_DOMAIN;

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
		final int index = login.indexOf(AT);
		if (index < 0) {
			return login + AT + server;
		} else {
			return login;
		}
	}

	@Nullable
	public String getDomain() {
		return getAfterAt(login);
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
	public String getLoginForConnection() {
		final String login;
		if (useLoginWithDomain) {
			login = getLogin();
		} else {
			login = getLoginWithoutDomain();
		}
		return login;
	}

	@Nonnull
	private String getLoginWithoutDomain() {
		return getBeforeAt(login);
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
	public XmppSecurityMode getSecurityMode() {
		return securityMode;
	}

	public void setSecurityMode(@Nonnull XmppSecurityMode securityMode) {
		this.securityMode = securityMode;
	}

	public boolean isUseLoginWithDomain() {
		return useLoginWithDomain;
	}

	public void setUseLoginWithDomain(boolean useLoginWithDomain) {
		this.useLoginWithDomain = useLoginWithDomain;
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
		final AndroidConnectionConfiguration configuration = new AndroidConnectionConfiguration(this.server, this.port, getDomain());

		if (App.isDebuggable()) {
			configuration.setDebuggerEnabled(DEBUG);
		}

		configuration.setSecurityMode(securityMode.toSmackMode());

		// we manually manage the connectivity (see https://github.com/serso/android-messengerpp/issues/62)
		configuration.setReconnectionAllowed(false);

		return configuration;
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
			if (!this.resource.equals(that.resource)) {
				return false;
			}

			if (!this.securityMode.equals(that.securityMode)) {
				return false;
			}

			if (this.useLoginWithDomain != that.useLoginWithDomain) {
				return false;
			}
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

	/*
	**********************************************************************
	*
	*                           STATIC
	*
	**********************************************************************
	*/

	@Nullable
	public static String getAfterAt(@Nonnull String s) {
		final int index = s.indexOf(AT);
		if (index < 0) {
			return null;
		} else {
			return s.substring(index + 1);
		}
	}

	@Nonnull
	public static String getBeforeAt(@Nonnull String s) {
		final int index = s.indexOf(AT);
		if (index < 0) {
			return s;
		} else {
			return s.substring(0, index);
		}
	}
}
