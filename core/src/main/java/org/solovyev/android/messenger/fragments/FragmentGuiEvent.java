package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:18 PM
 */
public class FragmentGuiEvent {

    @Nonnull
    private final FragmentGuiEventType type;

    @Nonnull
    private final Class<? extends Fragment> fragmentClass;

    private int parentViewId = View.NO_ID;

    FragmentGuiEvent(@Nonnull FragmentGuiEventType type, @Nonnull Class<? extends Fragment> fragmentClass) {
        this.type = type;
        this.fragmentClass = fragmentClass;
    }

    FragmentGuiEvent(@Nonnull FragmentGuiEventType type, @Nonnull Class<? extends Fragment> fragmentClass, int parentViewId) {
        this.type = type;
        this.fragmentClass = fragmentClass;
        this.parentViewId = parentViewId;
    }

    @Nonnull
    public FragmentGuiEventType getType() {
        return type;
    }

    @Nonnull
    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }

    public int getParentViewId() {
        return parentViewId;
    }
}

