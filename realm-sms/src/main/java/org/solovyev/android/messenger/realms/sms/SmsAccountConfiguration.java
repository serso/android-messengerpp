package org.solovyev.android.messenger.realms.sms;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:43 PM
 */
public final class SmsAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	@Override
	public SmsAccountConfiguration clone() {
		return (SmsAccountConfiguration) super.clone();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SmsAccountConfiguration;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
