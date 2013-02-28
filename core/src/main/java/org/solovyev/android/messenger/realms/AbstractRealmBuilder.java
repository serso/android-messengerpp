package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;

public abstract class AbstractRealmBuilder implements RealmBuilder {

    @NotNull
    private RealmDef realmDef;

    @Nullable
    private Realm editedRealm;

    protected AbstractRealmBuilder(@NotNull RealmDef realmDef, @Nullable Realm editedRealm) {
        this.realmDef = realmDef;
        this.editedRealm = editedRealm;
    }

    @NotNull
    @Override
    public final Realm build(@NotNull Data data) {
        final String realmId = data.getRealmId();

        User user = getUserById(realmId, data.getAuthData().getRealmUserId());
        if ( user == null ) {
            user = UserImpl.newFakeInstance(RealmEntityImpl.newInstance(realmId, data.getAuthData().getRealmUserId()));
        }

        return newRealm(realmId, user);
    }

    @Nullable
    protected abstract User getUserById(@NotNull String realmId, @NotNull String realmUserId);

    @NotNull
    public RealmDef getRealmDef() {
        return realmDef;
    }

    @Nullable
    @Override
    public Realm getEditedRealm() {
        return this.editedRealm;
    }

    @NotNull
    protected abstract Realm newRealm(@NotNull String id, @NotNull User user);
}
