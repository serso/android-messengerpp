package org.solovyev.android.messenger.preferences;

import org.solovyev.android.messenger.MessengerEntity;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 5:58 PM
 */
public final class PreferenceGroup implements MessengerEntity {

    @Nonnull
    private final String id;

    @Nonnull
    private final String name;

    private final int preferencesResId;

    public PreferenceGroup(@Nonnull String id, @Nonnull String name, int preferencesResId) {
        this.id = id;
        this.name = name;
        this.preferencesResId = preferencesResId;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    public CharSequence getName() {
        return this.name;
    }

    public int getPreferencesResId() {
        return preferencesResId;
    }
}
