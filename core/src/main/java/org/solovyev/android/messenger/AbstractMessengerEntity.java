package org.solovyev.android.messenger;

import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 2:26 PM
 */
public abstract class AbstractMessengerEntity extends JObject implements MessengerEntity {

    @Nonnull
    private /*final*/ RealmEntity realmEntity;

    protected AbstractMessengerEntity(@Nonnull RealmEntity realmEntity) {
        this.realmEntity = realmEntity;
    }

    @Nonnull
    @Override
    public final String getId() {
        return realmEntity.getEntityId();
    }

    @Nonnull
    public RealmEntity getRealmEntity() {
        return realmEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        final AbstractMessengerEntity that = (AbstractMessengerEntity) o;

        if (!realmEntity.equals(that.realmEntity)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return realmEntity.hashCode();
    }

    @Nonnull
    @Override
    public AbstractMessengerEntity clone() {
        final AbstractMessengerEntity clone = (AbstractMessengerEntity) super.clone();

        clone.realmEntity = realmEntity.clone();

        return clone;
    }
}
