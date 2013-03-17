package org.solovyev.android.messenger.preferences;

import org.solovyev.android.messenger.MessengerEntity;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 5:58 PM
 */
public final class PreferenceGroup implements MessengerEntity {

    private static final int NO_ICON = -1;

    @Nonnull
    private final String id;

    @Nonnull
    private final String name;

    private final int preferencesResId;

    private final int iconResId;

    public PreferenceGroup(@Nonnull String id, @Nonnull String name, int preferencesResId) {
        this.id = id;
        this.name = name;
        this.preferencesResId = preferencesResId;
        this.iconResId = NO_ICON;
    }

    public PreferenceGroup(@Nonnull String id, @Nonnull String name, int preferencesResId, int iconResId) {
        this.id = id;
        this.name = name;
        this.preferencesResId = preferencesResId;
        this.iconResId = iconResId;
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

    public boolean hasIcon() {
        return iconResId != NO_ICON;
    }

    public int getIconResId() {
        return iconResId;
    }
}
