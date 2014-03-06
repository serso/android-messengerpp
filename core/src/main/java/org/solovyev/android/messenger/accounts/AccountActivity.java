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

package org.solovyev.android.messenger.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.core.R;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.isBigScreen;
import static org.solovyev.android.messenger.accounts.Accounts.newAccountArguments;
import static org.solovyev.android.messenger.accounts.Accounts.newEditAccountArguments;
import static org.solovyev.android.messenger.accounts.BaseAccountConfigurationFragment.newEditAccountConfigurationFragmentDef;

public class AccountActivity extends BaseFragmentActivity {

	private final static String ARGS_BUNDLE = "bundle";
	private final static String ARGS_EDIT = "edit";
	private static final String TAG = App.newTag(AccountActivity.class.getSimpleName());

	public static void open(@Nonnull Activity activity, @Nonnull Account account, boolean edit) {
		final Intent intent = new Intent(activity, isBigScreen(activity) ? AccountActivity.Dialog.class : AccountActivity.class);
		intent.putExtra(ARGS_BUNDLE, edit ? newEditAccountArguments(account) : newAccountArguments(account));
		intent.putExtra(ARGS_EDIT, edit);
		Activities.startActivity(intent, activity);
	}

	public AccountActivity() {
		super(false, R.layout.mpp_main_one_pane);
	}

	public AccountActivity(boolean dialog, int layoutResId) {
		super(dialog, layoutResId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final Bundle arguments = intent.getBundleExtra(ARGS_BUNDLE);
		final boolean edit = intent.getBooleanExtra(ARGS_EDIT, false);
		if (arguments != null) {
			try {
				if (edit) {
					fragmentManager.setMainFragment(newEditAccountConfigurationFragmentDef(this, arguments, false));
				} else {
					fragmentManager.setMainFragment(AccountFragment.newAccountFragmentDef(this, arguments, false));
				}
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
				finish();
			}
		} else {
			Log.e(TAG, "Arguments must be provided for " + AccountActivity.class);
			finish();
		}
	}

	public static final class Dialog extends AccountActivity {
		public Dialog() {
			super(true, R.layout.mpp_dialog);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getListeners().add(AccountUiEvent.Saved.class, new EventListener<AccountUiEvent.Saved>() {
			@Override
			public void onEvent(AccountUiEvent.Saved event) {
				AccountActivity.this.finish();
			}
		});
	}
}
