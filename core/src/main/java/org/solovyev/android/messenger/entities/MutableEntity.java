package org.solovyev.android.messenger.entities;

import javax.annotation.Nonnull;

public interface MutableEntity extends Entity {

	void setAccountEntityId(@Nonnull String accountEntityId);

	@Nonnull
	@Override
	MutableEntity clone();
}
