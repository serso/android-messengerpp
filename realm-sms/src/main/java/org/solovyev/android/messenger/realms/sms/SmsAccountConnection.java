package org.solovyev.android.messenger.realms.sms;

import android.content.Context;
import org.solovyev.android.messenger.accounts.connection.AbstractAccountConnection;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:22 PM
 */
final class SmsAccountConnection extends AbstractAccountConnection<SmsAccount> {

	SmsAccountConnection(@Nonnull SmsAccount account, @Nonnull Context context) {
		super(account, context);
	}

	@Override
	protected void doWork() throws AccountConnectionException {

	}

	@Override
	protected void stopWork() {

	}
}
