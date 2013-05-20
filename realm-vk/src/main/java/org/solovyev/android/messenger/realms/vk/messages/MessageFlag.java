package org.solovyev.android.messenger.realms.vk.messages;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/26/12
 * Time: 1:36 AM
 */
public enum MessageFlag {

	unread(1),
	outbox(2),
	replied(4),
	important(8),
	chat(16),
	friends(32),
	spam(64),
	deleted(128),
	fixed(256),
	media(512);

	private final int mask;

	MessageFlag(int mask) {
		this.mask = mask;
	}

	public boolean isApplied(int flags) {
		// bitwise and
		return (mask & flags) == mask;
	}

	@Nonnull
	public static List<MessageFlag> getMessageFlags(int flags) {
		if (flags != 0) {
			final MessageFlag[] values = values();

			final List<MessageFlag> result = new ArrayList<MessageFlag>(values.length);

			for (MessageFlag messageFlag : values) {
				if (messageFlag.isApplied(flags)) {
					result.add(messageFlag);
				}
			}

			return result;
		} else {
			return Collections.emptyList();
		}
	}
}
