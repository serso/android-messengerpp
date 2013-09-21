package org.solovyev.android.messenger;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
