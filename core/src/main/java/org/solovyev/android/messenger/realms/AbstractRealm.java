package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

public abstract class AbstractRealm<C extends RealmConfiguration> implements Realm<C> {

    @NotNull
    private String id;

    @NotNull
    private RealmDef realmDef;

    @NotNull
    private User user;

    @NotNull
    private C configuration;

    public AbstractRealm(@NotNull String id,
                         @NotNull RealmDef realmDef,
                         @NotNull User user,
                         @NotNull C configuration) {
        this.id = id;
        this.realmDef = realmDef;
        this.user = user;
        this.configuration = configuration;
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
        return this.user;
    }

    @NotNull
    @Override
    public C getConfiguration() {
        return this.configuration;
    }

    @NotNull
    @Override
    public RealmEntity newRealmEntity(@NotNull String realmEntityId) {
        return RealmEntityImpl.newInstance(getId(), realmEntityId);
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

    @Override
    public boolean same(@NotNull Realm r) {
        if (r instanceof AbstractRealm) {
            final AbstractRealm that = (AbstractRealm) r;
            return this.configuration.equals(that.configuration);
        } else {
            return false;
        }
    }
}
