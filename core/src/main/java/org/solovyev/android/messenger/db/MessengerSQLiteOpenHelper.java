package org.solovyev.android.messenger.db;

import android.app.Application;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.db.CommonSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 11:45 PM
 */
@Singleton
public class MessengerSQLiteOpenHelper extends CommonSQLiteOpenHelper {

    @Inject
    public MessengerSQLiteOpenHelper(@NotNull Application application,
                                     @NotNull SQLiteOpenHelperConfiguration configuration) {
        super(application, configuration);
    }
}
