package org.solovyev.android.messenger.preferences;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:24 PM
 */
public enum PreferenceUiEventType {

	preference_group_clicked;


	@Nonnull
	public PreferenceUiEvent newEvent(@Nonnull PreferenceGroup preferenceGroup) {
		return new PreferenceUiEvent(preferenceGroup, this, null);
	}
}
