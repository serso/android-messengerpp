package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AbstractAccountConnectionTest {

	@Test
	public void testStartShouldBeCalledIfAlreadyStarted() throws Exception {
		final TestAccountConnection connection = new TestAccountConnection();
		connection.start();
		assertEquals(1, connection.startCounter.get());
		connection.start();
		assertEquals(2, connection.startCounter.get());
	}

	@Test
	public void testStartShouldBeCalledAfterStop() throws Exception {
		final TestAccountConnection connection = new TestAccountConnection();
		connection.start();
		assertEquals(1, connection.startCounter.get());

		connection.stop();
		assertEquals(1, connection.stopCounter.get());

		connection.start();
		assertEquals(2, connection.startCounter.get());
	}

	@Test
	public void testStopShouldBeCalledAfterStart() throws Exception {
		final TestAccountConnection connection = new TestAccountConnection();
		connection.start();
		assertEquals(1, connection.startCounter.get());

		connection.stop();
		assertEquals(1, connection.stopCounter.get());

		connection.start();
		assertEquals(2, connection.startCounter.get());

		connection.stop();
		assertEquals(2, connection.stopCounter.get());
	}

	@Test
	public void testStopShouldNotBeCalledIfStopped() throws Exception {
		final TestAccountConnection connection = new TestAccountConnection();
		connection.stop();
		assertEquals(0, connection.stopCounter.get());
	}

	private static final class TestAccountConnection extends AbstractAccountConnection {

		private AtomicInteger startCounter = new AtomicInteger(0);
		private AtomicInteger stopCounter = new AtomicInteger(0);


		public TestAccountConnection() {
			super(mock(Account.class), mock(Context.class), false);
		}

		@Override
		protected void start0() throws AccountConnectionException {
			startCounter.incrementAndGet();
		}

		@Override
		protected void stop0() {
			stopCounter.incrementAndGet();
		}
	}
}
