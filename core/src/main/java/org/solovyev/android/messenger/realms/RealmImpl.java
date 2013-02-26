package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

public class RealmImpl implements Realm {

    @NotNull
    private final String id;

    @NotNull
    private final RealmDef realmDef;

    public RealmImpl(@NotNull String id, @NotNull RealmDef realmDef) {
        this.id = id;
        this.realmDef = realmDef;
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public RealmDef getRealmDef() {
        return realmDef;
    }

    @NotNull
    @Override
    public User getUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmEntity newRealmEntity(@NotNull String realmEntityId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmConnection createRealmConnection(@NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmUserService getRealmUserService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmChatService getRealmChatService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmAuthService getRealmAuthService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmConnection newRealmConnection(@NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
