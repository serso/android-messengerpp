package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.common.JObject;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 2:26 PM
 */
public abstract class AbstractIdentifiable extends JObject implements Identifiable {

	@Nonnull
	private /*final*/ Entity entity;

	protected AbstractIdentifiable(@Nonnull Entity entity) {
		this.entity = entity;
	}

	@Nonnull
	@Override
	public final String getId() {
		return entity.getEntityId();
	}

	@Nonnull
	public Entity getEntity() {
		return entity;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}

		final AbstractIdentifiable that = (AbstractIdentifiable) o;

		if (!entity.equals(that.entity)) {
			return false;
		}

		return true;
	}

	@Override
	public final int hashCode() {
		return entity.hashCode();
	}

	@Nonnull
	@Override
	public AbstractIdentifiable clone() {
		final AbstractIdentifiable clone = (AbstractIdentifiable) super.clone();

		clone.entity = entity.clone();

		return clone;
	}
}
