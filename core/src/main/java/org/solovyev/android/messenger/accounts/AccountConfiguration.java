package org.solovyev.android.messenger.accounts;

import org.solovyev.common.JCloneable;

public interface AccountConfiguration extends JCloneable<AccountConfiguration> {

	boolean isSameAccount(AccountConfiguration c);

	boolean isSameCredentials(AccountConfiguration c);

	boolean isSame(AccountConfiguration c);

	// method copies system data from old configuration to this configuration
	// NOTE: system data is data which cannot be changed by user (e.g. authentication token)
	void applySystemData(AccountConfiguration oldConfiguration);
}
