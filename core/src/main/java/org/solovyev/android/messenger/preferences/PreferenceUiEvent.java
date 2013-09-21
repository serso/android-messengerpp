package org.solovyev.android.messenger.preferences;

import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:25 PM
 */
public final class PreferenceUiEvent extends AbstractTypedJEvent<PreferenceGroup, PreferenceUiEventType> {

	public PreferenceUiEvent(@Nonnull PreferenceGroup eventObject, @Nonnull PreferenceUiEventType type, @Nullable Object data) {
		super(eventObject, type, data);
	}

	@Nonnull
	public PreferenceGroup getPreferenceScreen() {
		return getEventObject();
	}
}
