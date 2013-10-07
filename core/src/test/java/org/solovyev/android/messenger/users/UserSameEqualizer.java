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
