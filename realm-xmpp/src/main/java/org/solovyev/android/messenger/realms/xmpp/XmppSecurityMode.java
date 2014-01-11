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

import javax.annotation.Nonnull;

import static org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;

public enum XmppSecurityMode {

	required(SecurityMode.required, R.string.mpp_xmpp_security_mode_required),
	enabled(SecurityMode.enabled, R.string.mpp_xmpp_security_mode_enabled),
	disabled(SecurityMode.disabled, R.string.mpp_xmpp_security_mode_disabled);

	@Nonnull
	private final SecurityMode smackMode;

	private final int nameResId;

	XmppSecurityMode(@Nonnull SecurityMode smackMode, int nameResId) {
		this.smackMode = smackMode;
		this.nameResId = nameResId;
	}

	@Nonnull
	public SecurityMode toSmackMode() {
		return smackMode;
	}

	public int getNameResId() {
		return nameResId;
	}
}
