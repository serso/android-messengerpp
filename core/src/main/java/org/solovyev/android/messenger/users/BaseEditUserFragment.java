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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.EditButtons;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.tasks.UserSaverCallable;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.accounts.tasks.UserSaverCallback.newUserSaverCallback;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;

public abstract class BaseEditUserFragment<A extends Account<?>> extends BaseUserFragment<A> {

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	@Nonnull
	private final EditButtons editButtons = new UserEditButtons<A>(this);

	protected BaseEditUserFragment(int layoutResId) {
		super(layoutResId);
	}

	@Nonnull
	public static MultiPaneFragmentDef newCreateUserFragmentDef(@Nonnull Context context, @Nonnull Account account, boolean addToBackStack) {
		final Bundle arguments = Users.newCreateUserArguments(account);
		return newEditUserFragmentDef(context, arguments, addToBackStack);
	}

	@Nonnull
	public static MultiPaneFragmentDef newEditUserFragmentDef(@Nonnull Context context, @Nonnull Account account, @Nonnull User user, boolean addToBackStack) {
		final Bundle arguments = Users.newEditUserArguments(account, user);
		return newEditUserFragmentDef(context, arguments, addToBackStack);
	}

	@Nonnull
	public static MultiPaneFragmentDef newEditUserFragmentDef(@Nonnull Context context, @Nonnull Bundle arguments, boolean addToBackStack) {
		Class<? extends BaseEditUserFragment> clazz = Users.getCreateUserFragmentClassFromArguments(arguments);
		if (clazz == null) {
			throw new IllegalArgumentException("Class must be set");
		}
		return MultiPaneFragmentDef.forClass(Users.CREATE_USER_FRAGMENT_TAG, addToBackStack, clazz, context, arguments);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		editButtons.onViewCreated(root);

		final User user = getUser();
		if (user != null) {
			getMultiPaneManager().showTitle(getSherlockActivity(), this, user.getDisplayName());
		} else {
			getMultiPaneManager().showTitle(getSherlockActivity(), this, getString(R.string.mpp_new_contact));
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		getTaskListeners().addTaskListener(UserSaverCallable.TASK_NAME, newUserSaverCallback(getFragmentActivity()), getActivity(), R.string.mpp_saving_user_title, R.string.mpp_saving_user_message);
	}

	@Override
	protected CharSequence getFragmentTitle() {
		return null;
	}

	@Nullable
	protected abstract MutableUser validateData();

	@Nonnull
	protected MutableUser getOrCreateUser() {
		final MutableUser result;

		final User user = getUser();
		if (user != null) {
			result = newUser(user.getEntity(), user.getProperties().getPropertiesCollection());
		} else {
			result = newEmptyUser(generateEntity(getAccount()));
		}

		return result;
	}
}
