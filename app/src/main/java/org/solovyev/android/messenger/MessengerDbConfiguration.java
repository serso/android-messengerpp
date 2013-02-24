package org.solovyev.android.messenger;

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
public class MessengerDbConfiguration implements SQLiteOpenHelperConfiguration {

    @NotNull
    @Override
    public String getName() {
        return MessengerApplication.DB_NAME;
    }

    @Override
    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return null;
    }

    @Override
    public int getVersion() {
        return MessengerApplication.DB_VERSION;
    }
}
