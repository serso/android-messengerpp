package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractRealm<C extends RealmConfiguration> implements Realm<C> {

    @Nonnull
    private String id;

    @Nonnull
    private RealmDef realmDef;

    @Nonnull
    private User user;

    @Nonnull
    private C configuration;

    /**
     * Last created realm connection
     */
    @Nullable
    private volatile RealmConnection realmConnection;

    public AbstractRealm(@Nonnull String id,
                         @Nonnull RealmDef realmDef,
                         @Nonnull User user,
                         @Nonnull C configuration) {
        if (!user.getEntity().getRealmId().equals(id)) {
            throw new IllegalArgumentException("User must belong to realm!");
        }

        this.id = id;
        this.realmDef = realmDef;
        this.user = user;
        this.configuration = configuration;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public RealmDef getRealmDef() {
        return realmDef;
    }

    @Nonnull
    @Override
    public User getUser() {
        return this.user;
    }

    @Nonnull
    @Override
    public C getConfiguration() {
        return this.configuration;
    }

    @Nonnull
    @Override
    public Entity newRealmEntity(@Nonnull String realmEntityId) {
        return EntityImpl.newInstance(getId(), realmEntityId);
    }

    @Nonnull
    @Override
    public Entity newRealmEntity(@Nonnull String realmEntityId, @Nonnull String entityId) {
        return EntityImpl.newInstance(getId(), realmEntityId, entityId);
    }

    @Nonnull
    @Override
    public Entity newUserEntity(@Nonnull String realmUserId) {
        return newRealmEntity(realmUserId);
    }

    @Nonnull
    @Override
    public Entity newChatEntity(@Nonnull String realmUserId) {
        return newRealmEntity(realmUserId);
    }

    @Nonnull
    @Override
    public Entity newMessageEntity(@Nonnull String realmMessageId) {
        return newRealmEntity(realmMessageId);
    }

    @Nonnull
    @Override
    public Entity newMessageEntity(@Nonnull String realmMessageId, @Nonnull String entityId) {
        return newRealmEntity(realmMessageId, entityId);
    }

    @Nonnull
    @Override
    public RealmAuthService getRealmAuthService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public final synchronized RealmConnection newRealmConnection(@Nonnull Context context) {
        final RealmConnection realmConnection = newRealmConnection0(context);
        this.realmConnection = realmConnection;
        return realmConnection;
    }

    @Nonnull
    protected abstract RealmConnection newRealmConnection0(@Nonnull Context context);

    @Nullable
    protected synchronized RealmConnection getRealmConnection() {
        return realmConnection;
    }

    @Override
    public boolean same(@Nonnull Realm r) {
        if (r instanceof AbstractRealm) {
            final AbstractRealm that = (AbstractRealm) r;
            return this.configuration.equals(that.configuration);
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRealm)) return false;

        final AbstractRealm that = (AbstractRealm) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
