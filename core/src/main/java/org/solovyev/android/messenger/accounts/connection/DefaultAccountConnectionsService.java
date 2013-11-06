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

package org.solovyev.android.messenger.accounts.connection;

import android.app.Application;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

import static org.solovyev.android.messenger.notifications.Notifications.NO_INTERNET_NOTIFICATION;
import static org.solovyev.android.messenger.notifications.Notifications.newAccountConnectionErrorNotification;

/**
 * User: serso
 * Date: 4/15/13
 * Time: 8:17 PM
 */
@Singleton
public final class DefaultAccountConnectionsService implements AccountConnectionsService, NetworkStateListener {

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
	@Inject
	@Nonnull
	private AccountConnections accountConnections;

	@Inject
	public DefaultAccountConnectionsService(@Nonnull Application context) {
		this.context = context;
	}

	@Override
	public void init() {
		networkStateService.addListener(this);

		accountService.addListener(new AccountEventListener());
		tryStartConnectionsFor(accountService.getEnabledAccounts());
	}

	void tryStartConnectionsFor(@Nonnull Collection<Account> accounts) {
		accountConnections.startConnectionsFor(accounts, isInternetConnectionExists());
	}

	boolean isInternetConnectionExists() {
		final NetworkData networkData = networkStateService.getNetworkData();
		return networkData.getState() == NetworkState.CONNECTED;
	}

	@Override
	public void onNetworkEvent(@Nonnull NetworkData networkData) {
		switch (networkData.getState()) {
			case CONNECTED:
				notificationService.remove(NO_INTERNET_NOTIFICATION);
				notificationService.remove(newAccountConnectionErrorNotification());
				accountConnections.tryStartAll(true);
				break;
			case UNKNOWN:
			case NOT_CONNECTED:
				notificationService.add(NO_INTERNET_NOTIFICATION);
				accountConnections.onNoInternetConnection();
				break;
		}
	}

	private final class AccountEventListener extends AbstractJEventListener<AccountEvent> implements JEventListener<AccountEvent> {

		private AccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull AccountEvent event) {
			final Account account = event.getAccount();
			switch (event.getType()) {
				case created:
					tryStartConnectionsFor(Arrays.asList(account));
					break;
				case configuration_changed:
				case changed:
					accountConnections.updateAccount(account, isInternetConnectionExists());
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

	void setAccountService(@Nonnull AccountService accountService) {
		this.accountService = accountService;
	}

	void setNetworkStateService(@Nonnull NetworkStateService networkStateService) {
		this.networkStateService = networkStateService;
	}

	void setAccountConnections(@Nonnull AccountConnections accountConnections) {
		this.accountConnections = accountConnections;
	}

	void setNotificationService(@Nonnull NotificationService notificationService) {
		this.notificationService = notificationService;
	}
}
