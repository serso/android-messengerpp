package org.solovyev.android.messenger;

import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 2:26 PM
 */
public abstract class AbstractMessengerEntity extends JObject implements MessengerEntity {

    @Nonnull
    private /*final*/ Entity entity;

    protected AbstractMessengerEntity(@Nonnull Entity entity) {
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

        final AbstractMessengerEntity that = (AbstractMessengerEntity) o;

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
    public AbstractMessengerEntity clone() {
        final AbstractMessengerEntity clone = (AbstractMessengerEntity) super.clone();

        clone.entity = entity.clone();

        return clone;
    }
}
