package org.solovyev.android.messenger.vk;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

/**
* User: serso
* Date: 8/13/12
* Time: 12:16 AM
*/
@Singleton
public class VkMessengerDbConfiguration implements SQLiteOpenHelperConfiguration {

    @NotNull
    @Override
    public String getName() {
        return VkMessengerApplication.DB_NAME;
    }

    @Override
    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return null;
    }

    @Override
    public int getVersion() {
        return VkMessengerApplication.DB_VERSION;
    }
}
