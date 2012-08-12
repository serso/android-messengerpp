package org.solovyev.android.messenger.users;

import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AProperty;
import org.solovyev.android.ext.StringVersionedEntityMapper;
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

        final UserSyncData userSyncData = UserSyncDataImpl.newInstanceFromStrings(c.getString(2), c.getString(3), c.getString(4), c.getString(5));

        final List<AProperty> properties;
        if (userDao != null) {
            properties = userDao.loadUserPropertiesById(versionedEntity.getId());
        } else {
            properties = Collections.emptyList();
        }

        return UserImpl.newInstance(versionedEntity, userSyncData, properties);
    }
}
