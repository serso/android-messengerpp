package org.solovyev.android.messenger.realms;

import android.content.Context;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

public interface Realm<C extends RealmConfiguration> {

    @Nonnull
    String getId();

    @Nonnull
    RealmDef getRealmDef();

    @Nonnull
    User getUser();

    @Nonnull
    C getConfiguration();

    @Nonnull
    RealmEntity newRealmEntity(@Nonnull String realmEntityId);

    @Nonnull
    RealmConnection createRealmConnection(@Nonnull Context context);

    boolean same(@Nonnull Realm that);

    @Nonnull
    String getDisplayName(@Nonnull Context context);

    /*
    **********************************************************************
    *
    *                           Realm Services
    *
    **********************************************************************
    */
    @Nonnull
    RealmUserService getRealmUserService();

    @Nonnull
    RealmChatService getRealmChatService();

    @Nonnull
    RealmAuthService getRealmAuthService();

    @Nonnull
    RealmConnection newRealmConnection(@Nonnull Context context);
}
