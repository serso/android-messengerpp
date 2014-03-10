package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.StringComparator;

import javax.annotation.Nonnull;
import java.util.Comparator;

class UserComparator implements Comparator<UiContact> {

	@Nonnull
	private final static Comparator<UiContact> instance = new UserComparator();

	@Nonnull
	private final Comparator<String> comparator = StringComparator.getInstance();

	@Nonnull
	public static Comparator<UiContact> getInstance() {
		return instance;
	}

	private UserComparator() {
	}

	@Override
	public int compare(UiContact lhs, UiContact rhs) {
		final String ldn = lhs.getDisplayName();
		final String rdn = rhs.getDisplayName();
		return comparator.compare(ldn, rdn);
	}
}
