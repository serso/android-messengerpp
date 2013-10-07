package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;

import org.solovyev.common.Objects;
import org.solovyev.common.equals.Equalizer;

public class AccountSameEqualizer implements Equalizer<Account> {
	@Override
	public boolean areEqual(@Nonnull Account a1, @Nonnull Account a2) {
		boolean same = Objects.areEqual(a1.getId(), a2.getId());
		same &= Objects.areEqual(a1.getRealm(), a2.getRealm());
		same &= Objects.areEqual(a1.getState(), a2.getState());
		same &= a1.getConfiguration().isSame(a2.getConfiguration());
		same &= Objects.areEqual(a1.getUser(), a2.getUser());
		return same;
	}
}
