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
