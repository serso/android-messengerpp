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

package org.solovyev.android.messenger.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.isBigScreen;
import static org.solovyev.android.messenger.users.BaseEditUserFragment.newEditUserFragmentDef;
import static org.solovyev.android.messenger.users.ContactFragment.newViewContactFragmentDef;
import static org.solovyev.android.messenger.users.Users.newEditUserArguments;
import static org.solovyev.android.messenger.users.Users.newUserArguments;

public class ContactActivity extends BaseFragmentActivity {

	private final static String ARGS_BUNDLE = "bundle";
	private final static String ARGS_EDIT = "edit";
	private static final String TAG = App.newTag(ContactActivity.class.getSimpleName());

	@Nonnull
	private String contactId;

	static void open(@Nonnull Activity activity, @Nonnull User contact, boolean edit) {
		final Account account = App.getAccountService().getAccountByEntity(contact.getEntity());
		final Intent intent = new Intent(activity, isBigScreen(activity) ? ContactActivity.Dialog.class : ContactActivity.class);
		intent.putExtra(ARGS_BUNDLE, edit ? newEditUserArguments(account, contact) : newUserArguments(account, contact));
		intent.putExtra(ARGS_EDIT, edit);
		Activities.startActivity(intent, activity);
	}

	public ContactActivity() {
		super(false, R.layout.mpp_main_one_pane);
	}

	public ContactActivity(boolean dialog, int layoutResId) {
		super(dialog, layoutResId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final Bundle arguments = intent.getBundleExtra(ARGS_BUNDLE);
		final boolean edit = intent.getBooleanExtra(ARGS_EDIT, false);
		if (arguments != null) {
			final String contactId = Users.getUserIdFromArguments(arguments);
			if (contactId != null) {
				try {
					this.contactId = contactId;
					if (edit) {
						fragmentManager.setMainFragment(newEditUserFragmentDef(this, arguments, false));
					} else {
						fragmentManager.setMainFragment(newViewContactFragmentDef(this, arguments, false));
					}
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e.getMessage(), e);
					finish();
				}
			} else {
				Log.e(TAG, "User id must be provided for " + ContactActivity.class);
				finish();
			}
		} else {
			Log.e(TAG, "Arguments must be provided for " + ContactActivity.class);
			finish();
		}
	}

	public static final class Dialog extends ContactActivity {
		public Dialog() {
			super(true, R.layout.mpp_dialog);
		}
	}

	@Override
	protected void onContactRemoved(@Nonnull String contactId) {
		if (this.contactId.equals(contactId)) {
			tryFinish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getListeners().add(ContactUiEvent.Edit.class, new ContactsActivity.EditContactListener(this));
	}
}
