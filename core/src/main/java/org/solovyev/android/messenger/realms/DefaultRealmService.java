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

package org.solovyev.android.messenger.realms;

import android.app.Application;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.Configuration;
import org.solovyev.android.messenger.accounts.AccountConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultRealmService implements RealmService {

	@Nonnull
	private final Map<String, Realm<? extends AccountConfiguration>> realms = new HashMap<String, Realm<? extends AccountConfiguration>>();

	@Nonnull
	private final Application context;

	@Inject
	public DefaultRealmService(@Nonnull Application context, @Nonnull Configuration configuration) {
		this(context, configuration.getRealms());
	}

	public DefaultRealmService(@Nonnull Application context, @Nonnull Collection<? extends Realm> realms) {
		this.context = context;
		for (Realm realm : realms) {
			this.realms.put(realm.getId(), realm);
		}
	}

	@Override
	public void init() {
		for (Realm realm : realms.values()) {
			realm.init(context);
		}
	}

	@Nonnull
	@Override
	public Collection<Realm<? extends AccountConfiguration>> getRealms() {
		return Collections.unmodifiableCollection(this.realms.values());
	}


	@Nonnull
	@Override
	public Realm<? extends AccountConfiguration> getRealmById(@Nonnull String realmId) throws UnsupportedRealmException {
		final Realm<? extends AccountConfiguration> realm = this.realms.get(realmId);
		if (realm == null) {
			throw new UnsupportedRealmException(realmId);
		}
		return realm;
	}
}
