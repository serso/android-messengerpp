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

package org.solovyev.android.messenger.accounts;

import org.solovyev.common.Objects;
import org.solovyev.common.equals.Equalizer;

import javax.annotation.Nonnull;

public class AccountSameEqualizer implements Equalizer<Account> {
	@Override
	public boolean areEqual(@Nonnull Account a1, @Nonnull Account a2) {
		boolean same = Objects.areEqual(a1.getId(), a2.getId());
		same &= Objects.areEqual(a1.getRealm(), a2.getRealm());
		same &= Objects.areEqual(a1.getState(), a2.getState());
		same &= a1.getConfiguration().isSame(a2.getConfiguration());
		same &= Objects.areEqual(a1.getUser(), a2.getUser());
		same &= Objects.areEqual(a1.getSyncData().getLastChatsSyncDate(), a2.getSyncData().getLastChatsSyncDate());
		same &= Objects.areEqual(a1.getSyncData().getLastContactsSyncDate(), a2.getSyncData().getLastContactsSyncDate());
		same &= Objects.areEqual(a1.getSyncData().getLastUserIconsSyncData(), a2.getSyncData().getLastUserIconsSyncData());
		return same;
	}
}
