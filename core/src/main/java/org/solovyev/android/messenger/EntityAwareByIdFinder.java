package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.entities.EntityAware;

import com.google.common.base.Predicate;

public class EntityAwareByIdFinder implements Predicate<EntityAware> {

	@Nonnull
	private final String entityId;

	public EntityAwareByIdFinder(@Nonnull String entityId) {
		this.entityId = entityId;
	}

	@Override
	public boolean apply(@Nullable EntityAware entityAware) {
		return entityAware != null && entityId.equals(entityAware.getEntity().getEntityId());
	}
}
