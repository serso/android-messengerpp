package org.solovyev.android.messenger.preferences;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:24 PM
 */
public enum PreferenceGuiEventType {

    preference_group_clicked;


    @Nonnull
    public PreferenceGuiEvent newEvent(@Nonnull PreferenceGroup preferenceGroup) {
        return new PreferenceGuiEvent(preferenceGroup, this, null);
    }
}
