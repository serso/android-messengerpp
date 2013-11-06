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

import javax.annotation.Nonnull;

public final class TestXmppConfiguration {

	public static final String USER_LOGIN = "messengerplusplus@gmail.com";
	public static final String USER_LOGIN2 = "messengerplusplus2@gmail.com";

	@Nonnull
	private final static XmppAccountConfiguration instance = new XmppAccountConfiguration("talk.google.com", USER_LOGIN, "Qwerty!@");

	@Nonnull
	private final static XmppAccountConfiguration instance2 = new XmppAccountConfiguration("talk.google.com", USER_LOGIN2, "Qwerty!@");

	private TestXmppConfiguration() {
	}

	@Nonnull
	public static XmppAccountConfiguration getInstance() {
		return instance;
	}

	@Nonnull
	public static XmppAccountConfiguration getInstance2() {
		return instance2;
	}
}
