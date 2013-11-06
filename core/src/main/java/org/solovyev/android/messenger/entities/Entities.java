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

package org.solovyev.android.messenger.entities;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.nanoTime;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;

public final class Entities {

	@Nonnull
	private static final AtomicLong lastId = new AtomicLong(0);

	public static final String DELIMITER = ":";

	private Entities() {
	}

	@Nonnull
	public static synchronized MutableEntity generateEntity(@Nonnull Account account) {
		final long id = lastId.get();

		long newId = nanoTime();
		if (id >= newId) {
			newId = id + 1;
		}

		lastId.set(newId);

		final Entity tmp = newEntity(account.getId(), String.valueOf(newId));


		// NOTE: empty account entity id in order to get real from realm service
		return newEntity(account.getId(), NO_ACCOUNT_ID, tmp.getEntityId());
	}

	@Nonnull
	public static String makeEntityId(@Nonnull String accountId, String appAccountEntityId) {
		return accountId + DELIMITER + appAccountEntityId;
	}

	@Nonnull
	public static MutableEntity newEntityFromEntityId(@Nonnull String entityId) {
		final int index = entityId.indexOf(DELIMITER);
		if (index >= 0) {
			final String accountId = entityId.substring(0, index);
			final String accountUserId = entityId.substring(index + 1);
			return newEntity(accountId, accountUserId);
		} else {
			throw new IllegalArgumentException("No account id is stored in entityId!");
		}
	}

	@Nonnull
	public static MutableEntity newEntity(@Nonnull String accountId, @Nonnull String accountEntityId, @Nonnull String entityId) {
		if (Strings.isEmpty(accountId)) {
			throw new IllegalArgumentException("Account cannot be empty!");
		}

		if (Strings.isEmpty(accountEntityId)) {
			throw new IllegalArgumentException("Account entity id cannot be empty!");
		}

		if (Strings.isEmpty(entityId)) {
			throw new IllegalArgumentException("Entity id cannot be empty!");
		}

		return new EntityImpl(accountId, accountEntityId, entityId);
	}

	@Nonnull
	public static MutableEntity newEntity(@Nonnull String accountId, @Nonnull String accountEntityId) {
		return newEntity(accountId, accountEntityId, makeEntityId(accountId, accountEntityId));
	}
}
