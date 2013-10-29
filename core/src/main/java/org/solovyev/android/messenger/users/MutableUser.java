package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.MutableEntity;
import org.solovyev.android.properties.MutableAProperties;

public interface MutableUser extends User {

	@Nonnull
	@Override
	MutableAProperties getProperties();

	@Nonnull
	MutableEntity getEntity();

	void setOnline(boolean online);

	void setLastName(@Nullable String lastName);

	void setFirstName(@Nullable String firstName);

	@Nonnull
	MutableUser clone();
}
