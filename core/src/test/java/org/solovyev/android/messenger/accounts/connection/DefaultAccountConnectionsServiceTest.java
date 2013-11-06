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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.notifications.Notifications;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.MutableObject;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.solovyev.android.messenger.accounts.AccountEventType.*;
import static org.solovyev.android.messenger.accounts.AccountsTest.newMockAccountWithStaticConnection;
import static org.solovyev.android.network.NetworkState.*;

@RunWith(RobolectricTestRunner.class)
public class DefaultAccountConnectionsServiceTest {

	@Nonnull
	private DefaultAccountConnectionsService service;

	@Nonnull
	private AccountConnections accountConnections;

	@Nonnull
	private NotificationService notificationService;

	@Nonnull
	private List<Account> accounts;

	@Nonnull
	private AccountService accountService;

	@Nonnull
	private NetworkData networkData;

	@Before
	public void setUp() throws Exception {
		service = new DefaultAccountConnectionsService(Robolectric.application);
		accountConnections = mock(AccountConnections.class);
		notificationService = mock(NotificationService.class);

		accounts = newAccounts();

		accountService = mock(AccountService.class);
		when(accountService.getEnabledAccounts()).thenReturn(accounts);

		final NetworkStateService networkService = mock(NetworkStateService.class);
		networkData = mock(NetworkData.class);
		when(networkData.getState()).thenReturn(CONNECTED);
		when(networkService.getNetworkData()).thenReturn(networkData);
		service.setAccountService(accountService);
		service.setNetworkStateService(networkService);
		service.setAccountConnections(accountConnections);
		service.setNotificationService(notificationService);
	}

	@Test
	public void testInitShouldStartConnections() throws Exception {
		service.init();
		verify(accountConnections, times(1)).startConnectionsFor(accounts, true);
	}

	@Nonnull
	private List<Account> newAccounts() {
		return Arrays.asList(newMockAccountWithStaticConnection(), newMockAccountWithStaticConnection());
	}

	@Test
	public void testShouldReturnFalseForUnknownConnection() throws Exception {
		when(networkData.getState()).thenReturn(UNKNOWN);
		assertFalse(service.isInternetConnectionExists());
	}

	@Test
	public void testShouldReturnFalseForNoConnection() throws Exception {
		when(networkData.getState()).thenReturn(NOT_CONNECTED);
		assertFalse(service.isInternetConnectionExists());
	}

	@Test
	public void testShouldStartConnectionsIfTryStartConnectionsForIsCalledWithUnknownConnection() throws Exception {
		when(networkData.getState()).thenReturn(UNKNOWN);
		service.tryStartConnectionsFor(accounts);
		verify(accountConnections, times(1)).startConnectionsFor(accounts, false);
	}

	@Test
	public void testShouldNotifyConnectionsIfNoInternet() throws Exception {
		int count = 0;
		for (NetworkState networkState : NetworkState.values()) {
			when(networkData.getState()).thenReturn(networkState);
			service.onNetworkEvent(networkData);
			if (networkState != CONNECTED) {
				count++;
				verify(accountConnections, times(count)).onNoInternetConnection();
			}
		}
	}

	@Test
	public void testShouldStartConnectionsIfInternetExists() throws Exception {
		for (NetworkState networkState : NetworkState.values()) {
			when(networkData.getState()).thenReturn(networkState);
			service.onNetworkEvent(networkData);
			if (networkState == CONNECTED) {
				verify(accountConnections, times(1)).tryStartAll(true);
			}
		}
	}

	@Test
	public void testShouldNotifyUserIfNoInternet() throws Exception {
		when(networkData.getState()).thenReturn(NOT_CONNECTED);
		service.onNetworkEvent(networkData);

		verify(notificationService, times(1)).add(Notifications.NO_INTERNET_NOTIFICATION);
	}

	@Test
	public void testShouldRemoveUserNotificationIfInternetExists() throws Exception {
		when(networkData.getState()).thenReturn(CONNECTED);
		service.onNetworkEvent(networkData);

		verify(notificationService, times(1)).remove(Notifications.NO_INTERNET_NOTIFICATION);
		verify(notificationService, times(1)).remove(Notifications.newAccountConnectionErrorNotification());
	}

	@Test
	public void testShouldStartConnectionForCreatedAccount() throws Exception {
		final JEventListener<AccountEvent> l = initServiceAndReturnListener();
		final Account account = newMockAccountWithStaticConnection();
		l.onEvent(created.newEvent(account, null));

		verify(accountConnections, times(1)).startConnectionsFor(Arrays.asList(account), true);
	}

	@Test
	public void testShouldNotifyConnectionsAboutChangedAccount() throws Exception {
		final JEventListener<AccountEvent> l = initServiceAndReturnListener();
		final Account account = newMockAccountWithStaticConnection();
		l.onEvent(changed.newEvent(account, null));

		verify(accountConnections, times(1)).updateAccount(account, true);

		l.onEvent(configuration_changed.newEvent(account, null));
		verify(accountConnections, times(2)).updateAccount(account, true);
	}

	@Test
	public void testShouldRemoveConnectionsForRemovedAccount() throws Exception {
		final JEventListener<AccountEvent> l = initServiceAndReturnListener();
		final Account account = newMockAccountWithStaticConnection();
		when(account.getState()).thenReturn(AccountState.removed);
		l.onEvent(state_changed.newEvent(account, null));

		verify(accountConnections, times(1)).removeConnectionFor(account);
	}

	@Test
	public void testShouldStopConnectionForStoppedAccount() throws Exception {
		final JEventListener<AccountEvent> l = initServiceAndReturnListener();
		final Account account = newMockAccountWithStaticConnection();
		l.onEvent(stop.newEvent(account, null));

		verify(accountConnections, times(1)).tryStopFor(account);
	}

	@Test
	public void testShouldStartConnectionForStartedAccount() throws Exception {
		final JEventListener<AccountEvent> l = initServiceAndReturnListener();
		final Account account = newMockAccountWithStaticConnection();
		l.onEvent(start.newEvent(account, null));

		verify(accountConnections, times(1)).startConnectionsFor(Arrays.asList(account), true);
	}

	@Nonnull
	private JEventListener<AccountEvent> initServiceAndReturnListener() {
		final MutableObject<JEventListener<AccountEvent>> listener = new MutableObject<JEventListener<AccountEvent>>();
		doAnswer(new Answer() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				listener.setObject((JEventListener<AccountEvent>) invocationOnMock.getArguments()[0]);
				return null;
			}
		}).when(accountService).addListener(any(JEventListener.class));
		service.init();

		assertNotNull(listener.getObject());

		return listener.getObject();
	}
}
