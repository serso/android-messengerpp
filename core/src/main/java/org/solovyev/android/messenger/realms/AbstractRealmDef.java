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
public abstract class AbstractRealmDef implements RealmDef {

    @NotNull
    private final String id;

    private final int nameResId;

    private final int iconResId;

    @NotNull
    private RealmUserService realmUserService;

    @NotNull
    private RealmChatService realmChatService;


    @NotNull
    private RealmAuthService realmAuthService;


    protected AbstractRealmDef(@NotNull String id,
                               int nameResId,
                               int iconResId, @NotNull RealmUserService realmUserService,
                               @NotNull RealmChatService realmChatService,
                               @NotNull RealmAuthService realmAuthService) {
        this.id = id;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
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

    @Override
    public int getNameResId() {
        return this.nameResId;
    }

    @Override
    public int getIconResId() {
        return this.iconResId;
    }

    @NotNull
    @Override
    public RealmAuthService getRealmAuthService() {
        return this.realmAuthService;
    }
}
