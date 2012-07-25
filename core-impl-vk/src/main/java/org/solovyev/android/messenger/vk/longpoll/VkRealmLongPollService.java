package org.solovyev.android.messenger.vk.longpoll;

import org.jetbrains.annotations.Nullable;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AndroidHttpUtils;
import org.solovyev.android.messenger.longpoll.RealmLongPollService;
import org.solovyev.android.messenger.longpoll.LongPollResult;

import java.io.IOException;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:19 AM
 */
public class VkRealmLongPollService implements RealmLongPollService {

    @Override
    public Object startLongPolling() {
        try {
            return AndroidHttpUtils.execute(new VkGetLongPollServerHttpTransaction());
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    @Override
    public LongPollResult waitForResult(@Nullable Object longPollingData) {
        try {
            if (longPollingData instanceof LongPollServerData) {
                return AndroidHttpUtils.execute(new VkGetLongPollingDataHttpTransaction((LongPollServerData) longPollingData));
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }
}
