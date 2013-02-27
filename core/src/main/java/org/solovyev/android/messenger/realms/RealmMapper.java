package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

public class RealmMapper implements Converter<Cursor, Realm> {

    @NotNull
    private final RealmService realmService;

    @NotNull
    private final UserService userService;

    public RealmMapper(@NotNull RealmService realmService, @NotNull UserService userService) {
        this.realmService = realmService;
        this.userService = userService;
    }

    @NotNull
    @Override
    public Realm convert(@NotNull Cursor cursor) {
        final String realmId = cursor.getString(0);
        final String realmDefId = cursor.getString(1);
        final String userId = cursor.getString(2);
        final String configuration = cursor.getString(3);

        final RealmDef realmDef = realmService.getRealmDefById(realmDefId);
        final User user = userService.getUserById(RealmEntityImpl.fromEntityId(userId));
        return realmDef.newRealm(realmId, user, new Gson().fromJson(configuration, realmDef.getConfigurationClass()));
    }
}
