package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:56 AM
 */
public interface RealmDef {

    @NotNull
    String FAKE_REALM_ID = "fake";

    // realm's identifier. Must be unique for all existed realms
    @NotNull
    String getId();

    int getNameResId();

    int getIconResId();

    @NotNull
    RealmUserService newRealmUserService(@NotNull Realm realm);

    @NotNull
    RealmChatService newRealmChatService(@NotNull Realm realm);

    @NotNull
    RealmAuthService newRealmAuthService(@NotNull Realm realm);

    @NotNull
    RealmConnection newRealmConnection(@NotNull Realm realm, @NotNull Context context);
}
