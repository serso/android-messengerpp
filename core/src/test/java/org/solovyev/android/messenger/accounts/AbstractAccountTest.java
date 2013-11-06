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

package org.solovyev.android.messenger.accounts;

import org.junit.Assert;
import org.junit.Test;
import org.solovyev.android.messenger.realms.TestRealm;

import static org.junit.Assert.assertNotSame;
import static org.solovyev.android.messenger.accounts.AccountState.disabled_by_app;
import static org.solovyev.android.messenger.accounts.AccountState.enabled;
import static org.solovyev.android.messenger.accounts.AccountsTest.assertEquals;

public class AbstractAccountTest {

	@Test
	public void testShouldCopyWithNewState() throws Exception {
		final TestAccount account = new TestAccount(new TestRealm(), 100);
		account.getConfiguration().setAnotherTestStringField("another");
		final Account copy = account.copyForNewState(disabled_by_app);

		assertNotSame(account, copy);
		assertEquals(account, copy, false);
		Assert.assertEquals(disabled_by_app, copy.getState());
		Assert.assertEquals(enabled, account.getState());
	}
}
