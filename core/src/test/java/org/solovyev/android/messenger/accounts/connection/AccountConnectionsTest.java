package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricTestRunner.class)
public class AccountConnectionsTest {

	@Nonnull
	private AccountConnections connections;

	@Nonnull
	private Account account;

	@Nonnull
	private AccountConnection connection;

	@Before
	public void setUp() throws Exception {
		connections = new AccountConnections(application);
		connections.setExecutor(new Executor() {
			@Override
			public void execute(@Nonnull Runnable command) {
				command.run();
			}
		});
		account = mock(Account.class);
		connection = newMockConnection();

		when(account.newConnection(any(Context.class))).thenReturn(connection);
		when(account.isEnabled()).thenReturn(true);
	}

	@Nonnull
	private AccountConnection newMockConnection() throws AccountConnectionException {
		final AccountConnection connection = mock(AccountConnection.class);

		when(connection.isStopped()).thenReturn(true);
		when(connection.isInternetConnectionRequired()).thenReturn(true);
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				when(connection.isStopped()).thenReturn(false);
				return null;
			}
		}).when(connection).start();

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				when(connection.isStopped()).thenReturn(true);
				return null;
			}
		}).when(connection).stop();
		when(connection.getAccount()).thenReturn(account);

		return connection;
	}

	@Test
	public void testShouldStartConnectionIfNetworkConnectionExists() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
	}

	@Test
	public void testShouldNotStartConnectionIfNetworkConnectionDoesntExist() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), false);
		verify(connection, times(0)).start();
	}

	@Test
	public void testShouldNotStartConnectionIfConnectionIsRunning() throws Exception {
		when(connection.isStopped()).thenReturn(false);
		connections.startConnectionsFor(Arrays.asList(account), false);
		verify(connection, times(0)).start();
	}

	@Test
	public void testShouldReuseConnectionIfExists() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(account, times(1)).newConnection(any(Context.class));
	}

	@Test
	public void testShouldRestartConnectionIfExistsAndStopped() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));
		connection.stop();

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(2)).start();
	}

	@Test
	public void testShouldNotRestartConnectionIfExistsAndNotStopped() throws Exception {
		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
		verify(account, times(1)).newConnection(any(Context.class));

		connections.startConnectionsFor(Arrays.asList(account), true);
		verify(connection, times(1)).start();
	}
}
