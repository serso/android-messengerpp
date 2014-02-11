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

package org.solovyev.android.messenger.realms.sms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import static org.solovyev.android.messenger.realms.sms.SmsRealm.INTENT_MMS_DELIVER;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.INTENT_SMS_DELIVER;
import static org.solovyev.common.Objects.areEqual;

public class SmsReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (areEqual(action, INTENT_SMS_DELIVER)) {
			ComponentName component = SmsApplication.getDefaultSmsApplication(context, false);
			if (component != null) {
				return component.getPackageName();
			}
		} else if (areEqual(action, INTENT_MMS_DELIVER)) {

		}
	}
}
