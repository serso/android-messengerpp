package org.solovyev.android.messenger.chats;

import android.database.Cursor;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Converter;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:27 PM
 */
public class ChatParticipantMapper implements Converter<Cursor, User> {

    @Nonnull
    private final UserService userService;

    public ChatParticipantMapper(@Nonnull UserService userService) {
        this.userService = userService;
    }

    @Nonnull
    @Override
    public User convert(@Nonnull Cursor cursor) {
        final String userId = cursor.getString(0);
        return userService.getUserById(RealmEntityImpl.fromEntityId(userId));
    }
}
