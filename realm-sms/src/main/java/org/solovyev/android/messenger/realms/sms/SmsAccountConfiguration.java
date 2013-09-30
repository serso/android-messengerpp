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

	private boolean stopFurtherProcessing = false;

	@Nonnull
	@Override
	public SmsAccountConfiguration clone() {
		return (SmsAccountConfiguration) super.clone();
	}

	@Override
	public boolean isSameAccount(AccountConfiguration c) {
		return c instanceof SmsAccountConfiguration;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		return isSameAccount(c);
	}

	@Override
	public boolean isSame(AccountConfiguration c) {
		boolean same = isSameCredentials(c);
		if(same) {
			final SmsAccountConfiguration that = (SmsAccountConfiguration) c;
			same = this.stopFurtherProcessing == that.stopFurtherProcessing;
		}
		return same;
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
	}

	public boolean isStopFurtherProcessing() {
		return stopFurtherProcessing;
	}

	void setStopFurtherProcessing(boolean stopFurtherProcessing) {
		this.stopFurtherProcessing = stopFurtherProcessing;
	}
}
