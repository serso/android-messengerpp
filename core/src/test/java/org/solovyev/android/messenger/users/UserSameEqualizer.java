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

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.PropertiesEqualizer;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.Equalizer;

public class UserSameEqualizer implements Equalizer<User> {

	@Override
	public boolean areEqual(@Nonnull User u1, @Nonnull User u2) {
		boolean same = Objects.areEqual(u1.getEntity(), u2.getEntity());

		same &= Objects.areEqual(u1.getDisplayName(), u2.getDisplayName());
		same &= Objects.areEqual(u1.getGender(), u2.getGender());

		same &= Objects.areEqual(u1.getProperties(), u2.getProperties(), new PropertiesEqualizer());

		same &= Objects.areEqual(u1.getUserSyncData().getLastChatsSyncDate(), u2.getUserSyncData().getLastChatsSyncDate());
		same &= Objects.areEqual(u1.getUserSyncData().getLastContactsSyncDate(), u2.getUserSyncData().getLastContactsSyncDate());
		same &= Objects.areEqual(u1.getUserSyncData().getLastPropertiesSyncDate(), u2.getUserSyncData().getLastPropertiesSyncDate());
		same &= Objects.areEqual(u1.getUserSyncData().getLastUserIconsSyncData(), u2.getUserSyncData().getLastUserIconsSyncData());

		return same;
	}
}
