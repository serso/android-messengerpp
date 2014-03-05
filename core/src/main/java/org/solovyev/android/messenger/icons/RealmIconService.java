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

package org.solovyev.android.messenger.icons;

import android.widget.ImageView;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.List;

public interface RealmIconService {

	void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

	void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView);

	/**
	 * Method fetches user icons for specified <var>users</var>
	 *
	 * @param users for which icon fetching must be done
	 */
	void fetchUsersIcons(@Nonnull List<User> users);

	void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView);
}
