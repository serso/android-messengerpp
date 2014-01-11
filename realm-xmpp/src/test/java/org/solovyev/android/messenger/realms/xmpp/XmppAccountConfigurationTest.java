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
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;
import static org.solovyev.android.messenger.realms.xmpp.XmppAccountConfiguration.getAfterAt;
import static org.solovyev.android.messenger.realms.xmpp.XmppSecurityMode.disabled;

public class XmppAccountConfigurationTest {

	@Test
	public void testShouldReturnNullIfNoAtExists() throws Exception {
		assertNull(getAfterAt("test"));
		assertNull(getAfterAt(""));
	}

	@Test
	public void testShouldReturnEmptyStringIfAtIsLastSymbol() throws Exception {
		assertEquals("", getAfterAt("test@"));
		assertEquals("", getAfterAt("@"));
		assertEquals("", getAfterAt("wetwegdfgsdfgsfdgsdfg@"));
	}

	@Test
	public void testShouldReturnEverythingAfterFirstAt() throws Exception {
		assertEquals("test2", getAfterAt("test@test2"));
		assertEquals("e", getAfterAt("@e"));
		assertEquals("qwer@trew", getAfterAt("test@qwer@trew"));
	}

	@Test
	public void testToJson() throws Exception {
		final XmppAccountConfiguration expected = newTestConfiguration();

		final XmppAccountConfiguration actual = toAndFromJson(expected);

		assertTrue(expected.isSame(actual));
	}

	@Nonnull
	private XmppAccountConfiguration toAndFromJson(@Nonnull XmppAccountConfiguration expected) {
		final Gson gson = new Gson();
		final String json = gson.toJson(expected);
		return gson.fromJson(json, XmppAccountConfiguration.class);
	}

	@Nonnull
	private static XmppAccountConfiguration newTestConfiguration() {
		final XmppAccountConfiguration expected = new XmppAccountConfiguration("server", "login", "password");
		expected.setPort(100);
		expected.setResource("M++");
		return expected;
	}

	@Test
	public void testShouldRestoreFromJson() throws Exception {
		final String json = "{\"server\":\"server\",\"login\":\"login\",\"password\":\"password\",\"resource\":\"M++\",\"port\":100}";

		final Gson gson = new Gson();
		final XmppAccountConfiguration actual = gson.fromJson(json, XmppAccountConfiguration.class);

		assertTrue(newTestConfiguration().isSame(actual));
	}

	@Test
	public void testShouldSaveSecurityMode() throws Exception {
		final XmppAccountConfiguration expected = newTestConfiguration();
		expected.setSecurityMode(disabled);

		final XmppAccountConfiguration actual = toAndFromJson(expected);

		assertTrue(expected.isSame(actual));
	}
}
