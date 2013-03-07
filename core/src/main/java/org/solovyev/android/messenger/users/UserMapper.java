package org.solovyev.android.messenger.users;

import android.database.Cursor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityMapper;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

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

    @Nonnull
    @Override
    public User convert(@Nonnull Cursor c) {
        final RealmEntity realmEntity = RealmEntityMapper.newInstanceFor(1).convert(c);

        final UserSyncData userSyncData = UserSyncDataImpl.newInstanceFromStrings(c.getString(3), c.getString(4), c.getString(5), c.getString(6));

        final List<AProperty> properties;
        if (userDao != null) {
            properties = userDao.loadUserPropertiesById(realmEntity.getEntityId());
        } else {
            properties = Collections.emptyList();
        }

        return Users.newUser(realmEntity, userSyncData, properties);
    }
}
