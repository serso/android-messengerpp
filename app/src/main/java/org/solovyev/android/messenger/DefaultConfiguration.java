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

package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.sms.SmsRealm;
import org.solovyev.android.messenger.realms.test.TestRealm;
import org.solovyev.android.messenger.realms.vk.VkRealm;
import org.solovyev.android.messenger.realms.xmpp.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.solovyev.android.messenger.App.isMonkeyRunner;

@Singleton
public class DefaultConfiguration implements Configuration {

	@Nonnull
	private final List<Realm> realms = new ArrayList<Realm>();

	@Inject
	@Nonnull
	private CustomXmppRealm xmppRealm;

	@Inject
	@Nonnull
	private FacebookXmppRealm facebookXmppRealm;

	@Inject
	@Nonnull
	private GoogleXmppRealm googleXmppRealm;

	@Inject
	@Nonnull
	private YandexXmppRealm yandexXmppRealm;

	@Inject
	@Nonnull
	private QipXmppRealm qipXmppRealm;

	@Inject
	@Nonnull
	private VkRealm vkRealm;

	@Inject
	@Nonnull
	private SmsRealm smsRealm;

	@Inject
	@Nonnull
	private TestRealm testRealm;

	public DefaultConfiguration() {
	}

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		synchronized (realms) {
			if (realms.isEmpty()) {
				realms.add(xmppRealm);
				realms.add(facebookXmppRealm);
				realms.add(googleXmppRealm);
				realms.add(yandexXmppRealm);
				realms.add(qipXmppRealm);
				realms.add(vkRealm);
				realms.add(smsRealm);
				if (isMonkeyRunner()) {
					realms.add(testRealm);
				}
			}
		}

		return this.realms;
	}
}
