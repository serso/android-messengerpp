package org.solovyev.android.messenger.accounts.connection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountState;

import javax.annotation.Nonnull;

import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.application;
import static org.solovyev.android.messenger.AppTest.mockApp;
import static org.solovyev.android.messenger.accounts.AccountsTest.newMockAccountWithStaticConnection;

@RunWith(RobolectricTestRunner.class)
public class ConnectionRunnableTest {

	@Nonnull
	private Account account;

	@Before
	public void setUp() throws Exception {
		mockApp();
		account = newMockAccountWithStaticConnection();
	}

	@Test
	public void testShouldStartConnectionIfAccountEnabled() throws Exception {
		final AccountConnection connection = account.newConnection(application);

		runConnection(connection);

		verify(connection, times(1)).start();
	}

	private void runConnection(@Nonnull AccountConnection connection) {
		final ConnectionRunnable runnable = new ConnectionRunnable(connection, 0);
		runnable.run();
	}

	@Test
	public void testShouldNotStartConnectionIfAccountDisabled() throws Exception {
		when(account.isEnabled()).thenReturn(false);
		final AccountConnection connection = account.newConnection(application);

		runConnection(connection);

		verify(connection, times(0)).start();
	}

	@Test
	public void testShouldNotStartConnectionOnlyIfNotStopped() throws Exception {
		final AccountConnection connection = account.newConnection(application);
		when(connection.isStopped()).thenReturn(false);

		runConnection(connection);

		verify(connection, times(0)).start();
	}

	@Test
	public void testRestartConnectionInCaseOfException() throws Exception {
		final AccountConnection connection = newBrokenConnection(account);

		runConnection(connection);

		verify(connection, atLeast(2)).start();
	}

	@Test
	public void testShouldDisableAccountInCaseOfException() throws Exception {
		final AccountConnection connection = newBrokenConnection(account);

		runConnection(connection);

		verify(App.getAccountService(), times(1)).changeAccountState(account, AccountState.disabled_by_app);
	}

	@Test
	public void testShouldStopConnectionInCaseOfException() throws Exception {
		final AccountConnection connection = newBrokenConnection(account);

		runConnection(connection);

		verify(connection, atLeast(1)).stop();
		Assert.assertTrue(connection.isStopped());
	}

	@Nonnull
	private AccountConnection newBrokenConnection(@Nonnull Account account) throws AccountConnectionException {
		final AccountConnection connection = account.newConnection(application);
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				when(connection.isStopped()).thenReturn(false);
				throw new RuntimeException("test");
			}
		}).when(connection).start();
		return connection;
	}


}
