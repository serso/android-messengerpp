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

package org.solovyev.android.messenger.chats;

import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.Equalizer;

import javax.annotation.Nonnull;

public final class ChatSameEqualizer implements Equalizer<Chat> {
	@Override
	public boolean areEqual(@Nonnull Chat c1, @Nonnull Chat c2) {
		boolean same = Objects.areEqual(c1.getEntity(), c2.getEntity());

		same &= Objects.areEqual(c1.getLastMessagesSyncDate(), c2.getLastMessagesSyncDate());

		same &= Objects.areEqual(c1.getPropertiesCollection(), c2.getPropertiesCollection(), new CollectionEqualizer<AProperty>(null));

		same &= Objects.areEqual(c1.isPrivate(), c2.isPrivate());
		same &= Objects.areEqual(c1.getSecondUser(), c2.getSecondUser());

		return same;
	}
}
