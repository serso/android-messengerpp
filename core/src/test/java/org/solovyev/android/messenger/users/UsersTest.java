package org.solovyev.android.messenger.users;

import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.collect.Iterables.contains;
import static org.solovyev.common.Objects.areEqual;

public class UsersTest {

	public static boolean areSame(@Nonnull User u1, @Nonnull User u2) {
		boolean same = areEqual(u1.getEntity(), u2.getEntity());
		same &= areEqual(u1.getDisplayName(), u2.getDisplayName());
		same &= areEqual(u1.getGender(), u2.getGender());

		same &= areSame(u1.getProperties(), u2.getProperties());

		same &= areEqual(u1.getUserSyncData().getLastChatsSyncDate(), u2.getUserSyncData().getLastChatsSyncDate());
		same &= areEqual(u1.getUserSyncData().getLastContactsSyncDate(), u2.getUserSyncData().getLastContactsSyncDate());
		same &= areEqual(u1.getUserSyncData().getLastPropertiesSyncDate(), u2.getUserSyncData().getLastPropertiesSyncDate());
		same &= areEqual(u1.getUserSyncData().getLastUserIconsSyncData(), u2.getUserSyncData().getLastUserIconsSyncData());

		return same;
	}

	private static boolean areSame(@Nonnull AProperties p1, @Nonnull AProperties p2) {
		final Collection<AProperty> c1 = p1.getPropertiesCollection();
		final Collection<AProperty> c2 = p2.getPropertiesCollection();
		if (c1.size() == c2.size()) {
			if (areSameOneWay(c1, c2)) {
				if (areSameOneWay(c2, c1)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean areSameOneWay(@Nonnull Collection<AProperty> c1, @Nonnull Collection<AProperty> c2) {
		for (AProperty p1 : c1) {
			if (!contains(c2, p1)) {
				return false;
			}
		}
		return true;
	}
}
