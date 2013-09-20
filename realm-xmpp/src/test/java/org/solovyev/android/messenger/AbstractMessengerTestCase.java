package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.realms.xmpp.XmppRealmDef;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
@RunWith(MessengerRobolectricTestRunner.class)
public abstract class AbstractMessengerTestCase {

	@Nonnull
	private Application application = Robolectric.application;

	@Nonnull
	@Inject
	private AccountService accountService;

	@Nonnull
	private TestMessengerModule module;

	@Inject
	@Nonnull
	private XmppRealmDef xmppRealmDef;

	@Before
	public void setUp() throws Exception {
		module = new TestMessengerModule(application);
		module.setUp(this, module);
	}

	@After
	public void tearDown() throws Exception {
		module.tearDown();
	}

	@Nonnull
	public Application getApplication() {
		return application;
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public XmppRealmDef getXmppRealmDef() {
		return xmppRealmDef;
	}
}
