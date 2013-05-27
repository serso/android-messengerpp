package org.solovyev.android.messenger.realms.sms;

import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:43 PM
 */
public final class SmsRealmConfiguration extends JObject implements RealmConfiguration {

	@Nonnull
	@Override
	public SmsRealmConfiguration clone() {
		return (SmsRealmConfiguration) super.clone();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SmsRealmConfiguration;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
