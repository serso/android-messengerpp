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
