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

package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.BaseAccountFragment;
import org.solovyev.common.listeners.AbstractJEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.users.Users.getUserIdFromArguments;

public abstract class BaseUserFragment<A extends Account<?>> extends BaseAccountFragment<A> {
	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private UserService userService;

	private User user;

	@Nullable
	private UserEventListener userEventListener;

	protected BaseUserFragment(int layoutResId) {
		super(layoutResId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments != null) {
			final String userId = getUserIdFromArguments(arguments);
			if (userId != null) {
				user = userService.getUserById(newEntityFromEntityId(userId));

				userEventListener = new UserEventListener();
				userService.addListener(userEventListener);
			}
		}
	}

	@Override
	public void onDestroy() {
		if (userEventListener != null) {
			userService.removeListener(userEventListener);
			userEventListener = null;
		}

		super.onDestroy();
	}

	protected boolean isNewUser() {
		return user == null;
	}

	public User getUser() {
		return user;
	}

	public void setUser(@Nonnull User user) {
		assert this.user != null && this.user.equals(user);
		this.user = user;
		onUserChanged(this.user);
	}

	protected void onUserChanged(@Nonnull User user) {
	}

	private final class UserEventListener extends AbstractJEventListener<UserEvent> {

		protected UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull UserEvent event) {
			final User user = event.getUser();
			final User fragmentUser = getUser();
			final A account = getAccount();

			if (fragmentUser != null) {
				switch (event.getType()) {
					case changed:
						if (fragmentUser.equals(user)) {
							setUser(user);
						}
						break;
					case contacts_changed:
						if (account.getUser().equals(user)) {
							for (User contact : event.getDataAsUsers()) {
								if (fragmentUser.equals(contact)) {
									setUser(contact);
									break;
								}
							}
						}
						break;
					case contact_removed:
						if (account.getUser().equals(user)) {
							final String contactId = event.getDataAsUserId();
							if (fragmentUser.getId().equals(contactId)) {
								final FragmentActivity activity = getActivity();
								if (activity != null) {
									if (getMultiPaneManager().isDualPane(activity)) {
										if (!getMultiPaneManager().isTriplePane(activity)) {
											getFragmentManager().popBackStack();
										}
									}
								}
							}
						}
						break;
				}
			}
		}
	}
}
