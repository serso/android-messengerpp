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

package org.solovyev.android.messenger.accounts;

import java.util.Map;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.entities.Entity;

import com.google.common.base.Predicate;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 9:00 PM
 */
public final class EntityMapEntryMatcher implements Predicate<Map.Entry<Entity, ?>> {

	@Nonnull
	private final String accountId;

	private EntityMapEntryMatcher(@Nonnull String accountId) {
		this.accountId = accountId;
	}

	@Nonnull
	public static EntityMapEntryMatcher forRealm(@Nonnull String realmId) {
		return new EntityMapEntryMatcher(realmId);
	}

	@Override
	public boolean apply(@javax.annotation.Nullable Map.Entry<Entity, ?> entry) {
		return entry != null && entry.getKey().getAccountId().equals(accountId);
	}
}
