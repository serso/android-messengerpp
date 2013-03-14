package org.solovyev.android.messenger.realms;

import android.database.Cursor;
import com.google.gson.Gson;
import javax.annotation.Nonnull;

import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

public class RealmMapper implements Converter<Cursor, Realm> {

    @Nonnull
    private final RealmService realmService;

    @Nonnull
    private final UserService userService;

    public RealmMapper(@Nonnull RealmService realmService, @Nonnull UserService userService) {
        this.realmService = realmService;
        this.userService = userService;
    }

    @Nonnull
    @Override
    public Realm convert(@Nonnull Cursor cursor) {
        final String realmId = cursor.getString(0);
        final String realmDefId = cursor.getString(1);
        final String userId = cursor.getString(2);
        final String configuration = cursor.getString(3);

        final RealmDef realmDef = realmService.getRealmDefById(realmDefId);
        // realm is not loaded => no way we can find user in realm services
        final User user = userService.getUserById(EntityImpl.fromEntityId(userId), false);
        return realmDef.newRealm(realmId, user, new Gson().fromJson(configuration, realmDef.getConfigurationClass()));
    }
}
