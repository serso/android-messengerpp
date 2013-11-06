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

package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.xmpp.*;

import javax.annotation.Nonnull;

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
