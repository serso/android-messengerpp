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
public class BaseAccountConnectionTest {

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

	private static final class TestAccountConnection extends BaseAccountConnection {

		private AtomicInteger startCounter = new AtomicInteger(0);
		private AtomicInteger stopCounter = new AtomicInteger(0);


		public TestAccountConnection() {
			super(mock(Account.class), mock(Context.class));
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
