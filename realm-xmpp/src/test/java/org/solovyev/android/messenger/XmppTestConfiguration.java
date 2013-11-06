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
import org.solovyev.android.messenger.realms.xmpp.CustomXmppRealm;

import javax.annotation.Nonnull;
import java.util.Collection;


@Singleton
public class XmppTestConfiguration extends TestConfiguration {

	@Inject
	@Nonnull
	private CustomXmppRealm realm;

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		final Collection<Realm> realms = super.getRealms();
		realms.add(realm);
		return realms;
	}
}
