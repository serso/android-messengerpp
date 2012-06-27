package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.database.Cursor;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.utils.Converter;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:27 PM
 */
public class ChatParticipantMapper implements Converter<Cursor, User> {

    @NotNull
    private final UserService userService;

    @NotNull
    private final Context context;

    public ChatParticipantMapper(@NotNull UserService userService, @NotNull Context context) {
        this.userService = userService;
        this.context = context;
    }

    @NotNull
    @Override
    public User convert(@NotNull Cursor cursor) {
        final Integer userId = cursor.getInt(0);
        return userService.getUserById(userId, context);
    }
}
