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
