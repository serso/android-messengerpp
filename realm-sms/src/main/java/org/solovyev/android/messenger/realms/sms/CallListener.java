package org.solovyev.android.messenger.realms.sms;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;

import javax.annotation.Nonnull;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static org.solovyev.android.messenger.App.getMainActivityClass;

class CallListener extends PhoneStateListener {

	@Nonnull
	private final Context context;

	private boolean enabled = false;

	public CallListener(@Nonnull Context context) {
		this.context = context;
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		switch (state) {
			case CALL_STATE_IDLE:
				if (enabled) {
					enabled = false;
					final Intent intent = new Intent(context, getMainActivityClass());
					intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
				break;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
