package org.solovyev.android.messenger.entities;

import org.solovyev.android.messenger.accounts.Account;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;
import static org.solovyev.android.messenger.entities.EntityImpl.newEntity;

public final class Entities {

	private Entities() {
	}

	@Nonnull
	public static synchronized Entity generateEntity(@Nonnull Account account) {
		// todo serso: create normal way of generating ids
		final Entity tmp = newEntity(account.getId(), String.valueOf(System.currentTimeMillis()));

		// NOTE: empty account entity id in order to get real from realm service
		return newEntity(account.getId(), NO_ACCOUNT_ID, tmp.getEntityId());
	}
}
