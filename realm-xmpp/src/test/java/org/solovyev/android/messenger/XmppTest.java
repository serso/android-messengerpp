package org.solovyev.android.messenger;

import android.app.Application;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.xmpp.*;

import com.google.inject.Inject;

public abstract class XmppTest extends DefaultMessengerTest {

	@Nonnull
	@Inject
	private CustomXmppRealm xmppRealm;

	@Nonnull
	private XmppAccount xmppAccount;

	@Nonnull
	@Override
	protected AbstractTestModule newModule(@Nonnull Application application) {
		return new XmppTestModule(application);
	}

	protected void populateDatabase() throws Exception {
		super.populateDatabase();
		xmppAccount = getAccountService().saveAccount(new XmppAccountBuilder(xmppRealm, null, XmppConfiguration.getInstance()));
	}

	@Nonnull
	public XmppRealm getXmppRealm() {
		return xmppRealm;
	}

	@Nonnull
	public XmppAccount getXmppAccount() {
		return xmppAccount;
	}
}
