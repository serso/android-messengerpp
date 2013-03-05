package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public abstract class AbstractRealm<C extends RealmConfiguration> implements Realm<C> {

    @Nonnull
    private String id;

    @Nonnull
    private RealmDef realmDef;

    @Nonnull
    private User user;

    @Nonnull
    private C configuration;

    public AbstractRealm(@Nonnull String id,
                         @Nonnull RealmDef realmDef,
                         @Nonnull User user,
                         @Nonnull C configuration) {
        if (!user.getRealmUser().getRealmId().equals(id)) {
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
    public RealmEntity newRealmEntity(@Nonnull String realmEntityId) {
        return RealmEntityImpl.newInstance(getId(), realmEntityId);
    }

    @Nonnull
    @Override
    public RealmAuthService getRealmAuthService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public RealmConnection newRealmConnection(@Nonnull Context context) {
        return getRealmDef().newRealmConnection(this, context);
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
