package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.entities.Entity;
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
    RealmState getState();

    boolean isEnabled();

    @Nonnull
    Entity newRealmEntity(@Nonnull String realmEntityId);

    @Nonnull
    Entity newRealmEntity(@Nonnull String realmEntityId, @Nonnull String entityId);

    @Nonnull
    Entity newUserEntity(@Nonnull String realmUserId);

    @Nonnull
    Entity newChatEntity(@Nonnull String realmUserId);

    @Nonnull
    Entity newMessageEntity(@Nonnull String realmMessageId);

    @Nonnull
    Entity newMessageEntity(@Nonnull String realmMessageId, @Nonnull String entityId);

    boolean same(@Nonnull Realm that);

    @Nonnull
    String getDisplayName(@Nonnull Context context);

    @Nonnull
    Realm copyForNewState(@Nonnull RealmState newState);

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
