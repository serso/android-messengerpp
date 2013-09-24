package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/23/13
 * Time: 5:18 PM
 */
final class MessengerEvent extends AbstractTypedJEvent<Integer, MessengerEventType> {

	MessengerEvent(@Nonnull MessengerEventType type, @Nullable Object data) {
		super(0, type, data);
	}

	@Nonnull
	public Integer getDataAsInteger() {
		return (Integer) getData();
	}
}
