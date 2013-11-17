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

package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import org.solovyev.android.fragments.FragmentDef;
import org.solovyev.android.messenger.accounts.AccountsFragment;
import org.solovyev.android.messenger.accounts.PickAccountFragment;
import org.solovyev.android.messenger.chats.ChatsFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.preferences.PreferenceGroupsFragment;
import org.solovyev.android.messenger.realms.RealmsFragment;
import org.solovyev.android.messenger.users.ContactsFragment;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;

public enum PrimaryFragment implements FragmentDef {

	contacts(ContactsFragment.class, Users.CONTACTS_FRAGMENT_TAG, R.string.mpp_tab_contacts),
	messages(ChatsFragment.class, CHATS_FRAGMENT_TAG, R.string.mpp_tab_messages),
	accounts(AccountsFragment.class, AccountsFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts),
	pick_account(PickAccountFragment.class, PickAccountFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts, false),
	realms(RealmsFragment.class, RealmsFragment.FRAGMENT_TAG, R.string.mpp_tab_accounts, true),
	settings(PreferenceGroupsFragment.class, PreferenceGroupsFragment.FRAGMENT_TAG, R.string.mpp_tab_preferences);

	@Nonnull
	private final Class<? extends Fragment> fragmentClass;

	@Nonnull
	private final String fragmentTag;

	private final int titleResId;

	private final boolean addToBackStack;

	PrimaryFragment(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull String fragmentTag, int titleResId) {
		this(fragmentClass, fragmentTag, titleResId, false);
	}

	private PrimaryFragment(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull String fragmentTag, int titleResId, boolean addToBackStack) {
		this.fragmentClass = fragmentClass;
		this.fragmentTag = fragmentTag;
		this.titleResId = titleResId;
		this.addToBackStack = addToBackStack;
	}

	@Override
	@Nonnull
	public String getFragmentTag() {
		return this.fragmentTag;
	}

	@Override
	@Nonnull
	public Class<? extends Fragment> getFragmentClass() {
		return fragmentClass;
	}

	public int getTitleResId() {
		return titleResId;
	}

	@Override
	public boolean isAddToBackStack() {
		return addToBackStack;
	}
}
