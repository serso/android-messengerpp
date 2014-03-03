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

package org.solovyev.android.messenger.accounts.tasks;


import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.tasks.ContextCallback;
import org.solovyev.android.tasks.Tasks;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.getEventManager;

public class UserSaverCallback implements ContextCallback<BaseFragmentActivity, User> {

	private UserSaverCallback() {
	}

	@Override
	public void onSuccess(@Nonnull BaseFragmentActivity activity, User user) {
		getEventManager(activity).fire(new ContactUiEvent.Saved(user));
	}

	@Override
	public void onFailure(@Nonnull BaseFragmentActivity context, Throwable t) {
	}

	public static FutureCallback<User> newUserSaverCallback(@Nonnull BaseFragmentActivity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new UserSaverCallback());
	}
}
