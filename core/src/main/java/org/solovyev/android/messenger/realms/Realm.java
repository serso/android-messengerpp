package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public interface Realm<C extends RealmConfiguration> extends MessengerEntity {

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
    RealmEntity newRealmEntity(@Nonnull String realmEntityId, @Nonnull String entityId);

    @Nonnull
    RealmEntity newUserEntity(@Nonnull String realmUserId);

    @Nonnull
    RealmEntity newMessageEntity(@Nonnull String realmMessageId);

    @Nonnull
    RealmEntity newMessageEntity(@Nonnull String realmMessageId, @Nonnull String entityId);

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
