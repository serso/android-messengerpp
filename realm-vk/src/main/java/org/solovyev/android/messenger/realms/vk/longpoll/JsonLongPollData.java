package org.solovyev.android.messenger.realms.vk.longpoll;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.http.IllegalJsonException;

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

	@Nonnull
	public VkLongPollResult toResult() throws IllegalJsonException {
		if (ts == null) {
			throw new IllegalJsonException();
		}

		return new VkLongPollResult(ts, updates == null ? Collections.<LongPollUpdate>emptyList() : updates);
	}
}
