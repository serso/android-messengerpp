package org.solovyev.android.messenger.preferences;

import org.solovyev.android.fragments.AbstractFragmentReuseCondition;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 8:00 PM
 */
public final class PreferenceListFragmentReuseCondition extends AbstractFragmentReuseCondition<PreferenceListFragment> {

    private final int preferenceResId;

    private PreferenceListFragmentReuseCondition(int preferenceResId) {
        super(PreferenceListFragment.class);
        this.preferenceResId = preferenceResId;
    }

    @Nonnull
    public static PreferenceListFragmentReuseCondition newInstance(int preferenceResId) {
        return new PreferenceListFragmentReuseCondition(preferenceResId);
    }

    @Override
    protected boolean canReuseFragment(@Nonnull PreferenceListFragment fragment) {
        return fragment.getPreferencesResId() == preferenceResId;
    }
}
