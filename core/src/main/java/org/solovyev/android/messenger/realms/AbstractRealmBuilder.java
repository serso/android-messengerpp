package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractRealmBuilder implements RealmBuilder {

    @Nonnull
    private RealmDef realmDef;

    @Nullable
    private Realm editedRealm;

    protected AbstractRealmBuilder(@Nonnull RealmDef realmDef, @Nullable Realm editedRealm) {
        this.realmDef = realmDef;
        this.editedRealm = editedRealm;
    }

    @Nonnull
    @Override
    public final Realm build(@Nonnull Data data) {
        final String realmId = data.getRealmId();

        User user = getUserById(realmId, data.getAuthData().getRealmUserId());
        if ( user == null ) {
            user = Users.newEmptyUser(EntityImpl.newInstance(realmId, data.getAuthData().getRealmUserId()));
        }

        return newRealm(realmId, user, RealmState.enabled);
    }

    @Nullable
    protected abstract User getUserById(@Nonnull String realmId, @Nonnull String realmUserId);

    @Nonnull
    public RealmDef getRealmDef() {
        return realmDef;
    }

    @Nullable
    @Override
    public Realm getEditedRealm() {
        return this.editedRealm;
    }

    @Nonnull
    protected abstract Realm newRealm(@Nonnull String id, @Nonnull User user, @Nonnull RealmState state);
}
