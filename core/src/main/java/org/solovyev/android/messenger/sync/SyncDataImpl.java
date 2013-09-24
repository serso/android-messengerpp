package org.solovyev.android.messenger.sync;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 6:25 PM
 */
public class SyncDataImpl implements SyncData {

	@Nonnull
	private final String accountId;

	public SyncDataImpl(@Nonnull String accountId) {
		this.accountId = accountId;
	}

	@Nonnull
	@Override
	public String getAccountId() {
		return this.accountId;
	}
}
