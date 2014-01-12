/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger.realms.xmpp;

import com.google.inject.Inject;
import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QipXmppAccountConfigurationFragment extends XmppAccountConfigurationFragment {

	@Inject
	@Nonnull
	private QipXmppRealm realm;

	@Nonnull
	@Override
	public Realm getRealm() {
		return realm;
	}

	@Nullable
	@Override
	protected String getServer() {
		return "webim.qip.ru";
	}

	@Nullable
	@Override
	protected String getDefaultDomain() {
		return "qip.ru";
	}

	@Override
	protected boolean isUseLoginWithDomain() {
		return false;
	}
}
