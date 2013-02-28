package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

public interface Realm<C extends RealmConfiguration> {

    @NotNull
    String getId();

    @NotNull
    RealmDef getRealmDef();

    @NotNull
    User getUser();

    @NotNull
    C getConfiguration();

    @NotNull
    RealmEntity newRealmEntity(@NotNull String realmEntityId);

    @NotNull
    RealmConnection createRealmConnection(@NotNull Context context);

    boolean same(@NotNull Realm that);

    @NotNull
    String getDisplayName(@NotNull Context context);

    /*
    **********************************************************************
    *
    *                           Realm Services
    *
    **********************************************************************
    */
    @NotNull
    RealmUserService getRealmUserService();

    @NotNull
    RealmChatService getRealmChatService();

    @NotNull
    RealmAuthService getRealmAuthService();

    @NotNull
    RealmConnection newRealmConnection(@NotNull Context context);
}
