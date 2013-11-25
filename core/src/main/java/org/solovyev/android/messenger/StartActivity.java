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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import roboguice.activity.RoboActivity;

import javax.annotation.Nonnull;

import static org.solovyev.common.Objects.areEqual;

public class StartActivity extends RoboActivity {

	private static final String INTENT_SHOW_UNREAD_MESSAGES_ACTION = "show_unread_messages";

	@Nonnull
	public static Intent newUnreadMessagesStartIntent(@Nonnull Context context) {
		final Intent intent = new Intent(context, StartActivity.class);
		intent.setAction(StartActivity.INTENT_SHOW_UNREAD_MESSAGES_ACTION);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		if (areEqual(intent.getAction(), INTENT_SHOW_UNREAD_MESSAGES_ACTION)) {
			MainActivity.startForUnreadMessages(this);
		} else {
			MainActivity.start(this);
		}

		// we must start service from here because Android can cache application
		// and Application#onCreate() is never called!
		App.startBackgroundService();
		App.getAccountConnectionsService().tryStartAll();

		this.finish();
	}
}
