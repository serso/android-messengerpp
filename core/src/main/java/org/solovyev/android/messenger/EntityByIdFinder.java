package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;

import com.google.common.base.Predicate;

public class EntityByIdFinder<E extends Entity> implements Predicate<E> {

	@Nonnull
	private final String entityId;

	public EntityByIdFinder(@Nonnull String entityId) {
		this.entityId = entityId;
	}

	@Override
	public boolean apply(@Nullable Entity entity) {
		return entity != null && entityId.equals(entity.getEntityId());
	}
}
