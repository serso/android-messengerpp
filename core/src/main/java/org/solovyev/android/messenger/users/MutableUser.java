package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.android.properties.MutableAProperties;

public interface MutableUser extends User {

	@Nonnull
	@Override
	MutableAProperties getProperties();

	@Nonnull
	MutableUser clone();

}
