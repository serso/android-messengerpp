package org.solovyev.android.messenger.vk.longpoll;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.longpoll.RealmLongPollService;
import org.solovyev.android.messenger.realms.Realm;

import java.io.IOException;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:19 AM
 */
public class VkRealmLongPollService implements RealmLongPollService {

    @Nonnull
    private final Realm realm;

    public VkRealmLongPollService(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Override
    public Object startLongPolling() {
        try {
            return HttpTransactions.execute(new VkGetLongPollServerHttpTransaction(realm));
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @Override
    public LongPollResult waitForResult(@Nullable Object longPollingData) {
        try {
            if (longPollingData instanceof LongPollServerData) {
                return HttpTransactions.execute(new VkGetLongPollingDataHttpTransaction((LongPollServerData) longPollingData));
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }
}
