package org.solovyev.android.messenger.realms;

import org.solovyev.common.JCloneable;

public interface AccountConfiguration extends JCloneable<AccountConfiguration> {

	int hashCode();

	boolean equals(Object o);
}
