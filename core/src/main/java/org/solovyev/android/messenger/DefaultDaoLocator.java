package org.solovyev.android.messenger;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.SqliteChatMessageDao;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 2:12 AM
 */
public class DefaultDaoLocator implements DaoLocator {

    @NotNull
    private SQLiteOpenHelper sqliteOpenHelper;

    public DefaultDaoLocator(@NotNull SQLiteOpenHelper sqliteOpenHelper) {
        this.sqliteOpenHelper = sqliteOpenHelper;
    }

    @NotNull
    @Override
    public UserDao getUserDao(@NotNull Context context) {
        return new SqliteUserDao(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public ChatDao getChatDao(@NotNull Context context) {
        return new SqliteChatDao(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public ChatMessageDao getChatMessageDao(@NotNull Context context) {
        return new SqliteChatMessageDao(context, sqliteOpenHelper);
    }
}
