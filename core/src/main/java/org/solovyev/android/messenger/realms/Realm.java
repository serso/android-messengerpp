package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

public interface Realm {

    @NotNull
    String getId();

    @NotNull
    RealmDef getRealmDef();

    @NotNull
    User getUser();

    @NotNull
    RealmEntity newRealmEntity(@NotNull String realmEntityId);

    @NotNull
    RealmConnection createRealmConnection(@NotNull Context context);

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
