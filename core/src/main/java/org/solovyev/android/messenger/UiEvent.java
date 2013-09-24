package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.listeners.AbstractTypedJEvent;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public final class UiEvent extends AbstractTypedJEvent<Integer, UiEventType> {

	public UiEvent(@Nonnull UiEventType type, @Nullable Object data) {
		super(0, type, data);
	}
}
