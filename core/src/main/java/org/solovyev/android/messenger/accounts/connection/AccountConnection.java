package org.solovyev.android.messenger.accounts.connection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:52 PM
 */

/**
 * Connection to remote realm.
 * <p/>
 * This class is often used in background and listens to remote events (e.g. implementing long polling).
 * To start listening one must call {@link AccountConnection#start()} method,
 * to finish listening - {@link AccountConnection#stop()}.
 * <p/>
 * Application can toggle state quite often (due, for example, to connectivity problems).
 */
public interface AccountConnection {

	@Nonnull
	Account getAccount();

	/**
	 * Method starts listening to remote realm events
	 */
	void start() throws AccountConnectionException;

	/**
	 * Method stops listening to remove realm events
	 */
	void stop();

	boolean isStopped();

	boolean isInternetConnectionRequired();
}
