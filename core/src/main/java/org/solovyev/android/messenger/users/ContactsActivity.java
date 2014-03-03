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
import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.realms.Realm;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUiHandler;
import static org.solovyev.android.messenger.users.ContactFragment.newViewContactFragmentDef;

public class ContactsActivity extends BaseFragmentActivity {

	public static void start(@Nonnull Activity activity) {
		final Intent intent = new Intent();
		intent.setClass(activity, ContactsActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// first time
			getMultiPaneFragmentManager().setMainFragment(PrimaryFragment.contacts);
		}

		initFragments();
	}

	@Override
	protected void onContactRemoved(@Nonnull String contactId) {
		if (!isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof BaseUserFragment) {
				final BaseUserFragment uf = (BaseUserFragment) fragment;
				final User user = uf.getUser();

				if (user.getId().equals(contactId)) {
					tryGoBack();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		final RoboListeners listeners = getListeners();

		listeners.add(ContactUiEvent.Clicked.class, new EventListener<ContactUiEvent.Clicked>() {
			@Override
			public void onEvent(ContactUiEvent.Clicked event) {
				final User contact = event.contact;
				final Account account = App.getAccountService().getAccountByEntity(contact.getEntity());

				// fix for EventManager. Event manager doesn't support removal/creation of listeners in onEvent() method => let's do it on the next main loop cycle
				getUiHandler().post(new Runnable() {
					@Override
					public void run() {
						if (isDualPane()) {
							fragmentManager.setSecondFragment(newViewContactFragmentDef(ContactsActivity.this, account, contact.getEntity(), false));
							if (isTriplePane()) {
								fragmentManager.emptifyThirdFragment();
							}
						} else {
							fragmentManager.setMainFragment(newViewContactFragmentDef(ContactsActivity.this, account, contact.getEntity(), true));
						}
					}
				});
			}
		});

		listeners.add(ContactUiEvent.Edit.class, new EditContactListener(this));
		listeners.add(ContactUiEvent.OpenChat.class, new EventListener<ContactUiEvent.OpenChat>() {
			@Override
			public void onEvent(ContactUiEvent.OpenChat event) {
				final User contact = event.contact;
				final Account account = event.account;

				new MessengerAsyncTask<Void, Void, Chat>() {

					@Override
					protected Chat doWork(@Nonnull List<Void> params) {
						Chat result = null;

						try {
							result = getChatService().getOrCreatePrivateChat(account.getUser().getEntity(), contact.getEntity());
						} catch (AccountException e) {
							throwException(e);
						}

						return result;
					}

					@Override
					protected void onSuccessPostExecute(@Nullable Chat chat) {
						if (chat != null) {
							MainActivity.startForChat(ContactsActivity.this, chat);
						}
					}

				}.executeInParallel();
			}
		});

		listeners.add(UiEvent.class, new EventListener<UiEvent>() {
			@Override
			public void onEvent(UiEvent event) {
				switch (event.getType()) {
					case new_contact:
						NewContactActivity.start(ContactsActivity.this);
						break;
				}
			}
		});

		listeners.add(ContactUiEvent.Saved.class, new EventListener<ContactUiEvent.Saved>() {
			@Override
			public void onEvent(ContactUiEvent.Saved event) {
				fragmentManager.goBackImmediately();
			}
		});
	}

	static class EditContactListener implements EventListener<ContactUiEvent.Edit> {

		@Nonnull
		private final Activity activity;

		EditContactListener(@Nonnull Activity activity) {
			this.activity = activity;
		}

		@Override
		public void onEvent(ContactUiEvent.Edit event) {
			final Account account = event.account;

			final Realm realm = account.getRealm();
			if (realm.canCreateUsers()) {
				ContactActivity.open(activity, event.contact, true);
			}
		}
	}
}
