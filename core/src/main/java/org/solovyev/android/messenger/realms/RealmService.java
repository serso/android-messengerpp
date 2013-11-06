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

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountConfiguration;

public interface RealmService {

	void init();

	/**
	 * @return collection of all configured realms in application
	 */
	@Nonnull
	Collection<Realm<? extends AccountConfiguration>> getRealms();

	/**
	 * Method returns the realm which previously has been registered in this service
	 *
	 *
	 * @param realmId id of realm
	 * @return realm
	 * @throws UnsupportedRealmException if realm hasn't been registered in this service
	 */
	@Nonnull
	Realm<? extends AccountConfiguration> getRealmById(@Nonnull String realmId) throws UnsupportedRealmException;
}
