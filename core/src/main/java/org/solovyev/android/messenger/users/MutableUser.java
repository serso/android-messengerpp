package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.MutableEntity;
import org.solovyev.android.properties.MutableAProperties;

public interface MutableUser extends User {

	@Nonnull
	@Override
	MutableAProperties getProperties();

	@Nonnull
	MutableEntity getEntity();

	@Nonnull
	MutableUser clone();

}
