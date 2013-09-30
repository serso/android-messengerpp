package org.solovyev.android.messenger.accounts.connection;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.accounts.Account;

@ThreadSafe
public interface AccountConnections {

	void startConnectionsFor(@Nonnull Collection<Account> accounts, boolean internetConnectionExists);

	void tryStopAll();

	void onNoInternetConnection();

	void tryStopFor(@Nonnull Account account);

	void tryStartAll(boolean internetConnectionExists);

	void removeConnectionFor(@Nonnull Account account);

	void updateAccount(@Nonnull Account account, boolean internetConnectionExists);
}
