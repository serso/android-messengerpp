package org.solovyev.android.messenger.users;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.db.StringVersionedEntityMapper;
import org.solovyev.android.messenger.realms.RealmUserImpl;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;
import org.solovyev.common.VersionedEntity;

import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 7:03 PM
 */
public class UserMapper implements Converter<Cursor, User> {

    @Nullable
    private final UserDao userDao;

    public UserMapper(@Nullable UserDao userDao) {
        this.userDao = userDao;
    }

    @NotNull
    @Override
    public User convert(@NotNull Cursor c) {
        final VersionedEntity<String> versionedEntity = StringVersionedEntityMapper.getInstance().convert(c);

        final String realmId = c.getString(2);
        final String realmUserId = c.getString(3);

        final UserSyncData userSyncData = UserSyncDataImpl.newInstanceFromStrings(c.getString(4), c.getString(5), c.getString(6), c.getString(7));

        final List<AProperty> properties;
        if (userDao != null) {
            properties = userDao.loadUserPropertiesById(versionedEntity.getId());
        } else {
            properties = Collections.emptyList();
        }

        return UserImpl.newInstance(versionedEntity, RealmUserImpl.newInstance(realmId, realmUserId), userSyncData, properties);
    }
}
