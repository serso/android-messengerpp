package org.solovyev.android.messenger.vk.longpoll;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.http.IllegalJsonException;

import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:52 AM
 */
public class JsonLongPollData {

    @Nullable
    private Long ts;

    @Nullable
    private List<LongPollUpdate> updates;

    @NotNull
    public VkLongPollResult toResult() throws IllegalJsonException {
         if ( ts == null) {
             throw new IllegalJsonException();
         }

        return new VkLongPollResult(ts, updates == null ? Collections.<LongPollUpdate>emptyList() : updates);
    }
}
