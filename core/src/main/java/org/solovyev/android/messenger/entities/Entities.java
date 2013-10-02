package org.solovyev.android.messenger.entities;

import org.solovyev.android.messenger.accounts.Account;

import javax.annotation.Nonnull;

import static java.lang.System.currentTimeMillis;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;

public final class Entities {

	public static final String DELIMITER = ":";

	private Entities() {
	}

	@Nonnull
	public static synchronized Entity generateEntity(@Nonnull Account account) {
		// todo serso: create normal way of generating ids
		final Entity tmp = EntityImpl.newEntity(account.getId(), String.valueOf(currentTimeMillis()));

		// NOTE: empty account entity id in order to get real from realm service
		return EntityImpl.newEntity(account.getId(), NO_ACCOUNT_ID, tmp.getEntityId());
	}

	@Nonnull
	public static String makeEntityId(@Nonnull String accountId, String appAccountEntityId) {
		return accountId + DELIMITER + appAccountEntityId;
	}

	@Nonnull
	public static Entity newEntityFromEntityId(@Nonnull String entityId) {
		final int index = entityId.indexOf(DELIMITER);
		if (index >= 0) {
			final String accountId = entityId.substring(0, index);
			final String accountUserId = entityId.substring(index + 1);
			return EntityImpl.newEntity(accountId, accountUserId);
		} else {
			throw new IllegalArgumentException("No account id is stored in entityId!");
		}
	}

}
