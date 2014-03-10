package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import java.util.Comparator;

public class StringComparator implements Comparator<String> {

	@Nonnull
	private final static Comparator<String> instance = new StringComparator();

	@Nonnull
	public static Comparator<String> getInstance() {
		return instance;
	}

	private StringComparator() {
	}

	@Override
	public int compare(String lhs, String rhs) {
		return compareStrings(lhs, rhs);
	}

	public static int compareStrings(@Nonnull String lhs, @Nonnull String rhs) {
		final int ll = lhs.length();
		final int rl = rhs.length();

		if (ll == 0 && rl == 0) {
			return 0;
		} else if (rl == 0) {
			return -1;
		} else if (ll == 0) {
			return 1;
		} else {
			final boolean lletter = Character.isLetter(lhs.charAt(0));
			final boolean rletter = Character.isLetter(rhs.charAt(0));
			if (lletter && rletter) {
				return lhs.compareToIgnoreCase(rhs);
			} else if (lletter) {
				return -1;
			} else if (rletter) {
				return 1;
			} else {
				return lhs.compareToIgnoreCase(rhs);
			}
		}
	}
}
