package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:05 AM
 */
public abstract class AbstractRealm implements Realm {

    @NotNull
    private final String id;

    @NotNull
    private RealmUserService realmUserService;

    @NotNull
    private RealmChatService realmChatService;


    @NotNull
    private RealmAuthService realmAuthService;


    protected AbstractRealm(@NotNull String id,
                            @NotNull RealmUserService realmUserService,
                            @NotNull RealmChatService realmChatService,
                            @NotNull RealmAuthService realmAuthService) {
        this.id = id;
        this.realmUserService = realmUserService;
        this.realmChatService = realmChatService;
        this.realmAuthService = realmAuthService;
    }

    @Override
    @NotNull
    public RealmUserService getRealmUserService() {
        return realmUserService;
    }

    @Override
    @NotNull
    public RealmChatService getRealmChatService() {
        return realmChatService;
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public RealmAuthService getRealmAuthService() {
        return this.realmAuthService;
    }
}
