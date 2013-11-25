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

import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;

public interface UserIconsService {
	/**
	 * Method sets icon of <var>user</var> in <var>imageView</var>
	 *
	 * @param user      user for whom icon shall be set
	 * @param imageView view to which icon shall be set
	 */
	void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method sets some icon which represents set of <var>users</var> in <var>imageView</var>
	 *
	 * @param account     realm
	 * @param users     users for whom icon shall be set
	 * @param imageView view to which icon shall be set
	 */
	void setUsersIcon(@Nonnull Account account, @Nonnull List<User> users, ImageView imageView);

	void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method fetches user icons for specified <var>account</var>
	 *
	 * @param account for which icon fetching must be done
	 */
	void fetchUserAndContactsIcons(@Nonnull Account account) throws UnsupportedAccountException;
}
