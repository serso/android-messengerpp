package org.solovyev.android.messenger.accounts.connection;

import android.app.Application;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.solovyev.android.messenger.notifications.Notifications.NO_INTERNET_NOTIFICATION;
import static org.solovyev.android.messenger.notifications.Notifications.newRealmConnectionErrorNotification;

/**
 * User: serso
 * Date: 4/15/13
 * Time: 8:17 PM
 */
@Singleton
public final class AccountConnectionsServiceImpl implements AccountConnectionsService, NetworkStateListener {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private MessengerListeners messengerListeners;

	@Inject
	@Nonnull
	private NotificationService notificationService;

	@Nonnull
	private final Application context;

	/*
	**********************************************************************
	*
	*                           OWN FIELDS
	*
	**********************************************************************
	*/
	@Nonnull
	private AccountConnections accountConnections;

	@Nullable
	private RealmEventListener realmEventListener;

	@Inject
	public AccountConnectionsServiceImpl(@Nonnull Application context) {
		this.context = context;
	}

	@Override
	public void init() {
		accountConnections = new AccountConnections(context);

		networkStateService.addListener(this);

		realmEventListener = new RealmEventListener();
		accountService.addListener(realmEventListener);

		tryStartConnectionsFor(accountService.getEnabledAccounts());
	}

	private void tryStartConnectionsFor(@Nonnull Collection<Account> accounts) {
		final boolean internetConnectionExists = canStartConnection();
		accountConnections.startConnectionsFor(accounts, internetConnectionExists);
	}

	private boolean canStartConnection() {
		final NetworkData networkData = networkStateService.getNetworkData();
		return networkData.getState() == NetworkState.CONNECTED;
	}

	@Override
	public void onNetworkEvent(@Nonnull NetworkData networkData) {
		switch (networkData.getState()) {
			case UNKNOWN:
				break;
			case CONNECTED:
				notificationService.remove(NO_INTERNET_NOTIFICATION);
				notificationService.remove(newRealmConnectionErrorNotification());
				accountConnections.tryStartAll();
				break;
			case NOT_CONNECTED:
				notificationService.add(NO_INTERNET_NOTIFICATION);
				accountConnections.onNoInternetConnection();
				break;
		}
	}

	private final class RealmEventListener extends AbstractJEventListener<AccountEvent> implements JEventListener<AccountEvent> {

		private RealmEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account account = event.getAccount();
			switch (event.getType()) {
				case created:
					tryStartConnectionsFor(Arrays.asList(account));
					break;
				case changed:
					accountConnections.updateAccount(account, canStartConnection());
					break;
				case state_changed:
					switch (account.getState()) {
						case removed:
							accountConnections.removeConnectionFor(account);
							break;
						default:
							if (account.isEnabled()) {
								tryStartConnectionsFor(Arrays.asList(account));
							} else {
								accountConnections.tryStopFor(account);
							}
							break;
					}
					break;
				case stop:
					accountConnections.tryStopFor(account);
					break;
				case start:
					tryStartConnectionsFor(Arrays.asList(account));
					break;
			}
		}
	}
}
