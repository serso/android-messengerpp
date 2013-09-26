package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Accounts {

	@Nonnull
	static Account newMockAccountWithStaticConnection() {
		final Account account = mock(Account.class);
		when(account.isEnabled()).thenReturn(true);
		prepareStaticConnectionForAccount(account);
		return account;
	}

	@Nonnull
	static AccountConnection prepareStaticConnectionForAccount(@Nonnull final Account account) {
		final AccountConnection connection = newMockConnection(account);
		when(account.newConnection(any(Context.class))).thenReturn(connection);
		return connection;
	}

	@Nonnull
	private static Account newMockAccount() {
		final Account account = mock(Account.class);
		when(account.isEnabled()).thenReturn(true);
		prepareConnectionForAccount(account);
		return account;
	}

	private static void prepareConnectionForAccount(@Nonnull final Account account) {
		when(account.newConnection(any(Context.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return newMockConnection(account);
			}
		});
	}

	@Nonnull
	private static AccountConnection newMockConnection(@Nonnull Account account) {
		final AccountConnection connection = mock(AccountConnection.class);

		when(connection.isStopped()).thenReturn(true);
		when(connection.isInternetConnectionRequired()).thenReturn(true);
		try {
			doAnswer(new Answer() {
				@Override
				public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
					when(connection.isStopped()).thenReturn(false);
					return null;
				}
			}).when(connection).start();
		} catch (AccountConnectionException e) {
			throw new AssertionError(e);
		}

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
}
