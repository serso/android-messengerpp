package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:22 PM
 */
public final class Realms {

	public static final String DELIMITER_REALM = "~";

	private Realms() {
		throw new AssertionError();
	}

	@Nonnull
	public static String makeAccountId(@Nonnull String realmId, int index) {
		return realmId + DELIMITER_REALM + index;
	}
}
