package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import org.solovyev.android.messenger.events.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:18 PM
 */
public final class FragmentGuiEvent extends AbstractTypedJEvent<Class<? extends Fragment>, FragmentGuiEventType> {


    FragmentGuiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentGuiEventType type) {
        super(fragmentClass, type, View.NO_ID);
    }

    FragmentGuiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentGuiEventType type, int parentViewId) {
        super(fragmentClass, type, parentViewId);
    }

    @Nonnull
    public Class<? extends Fragment> getFragmentClass() {
        return getEventObject();
    }

    public int getParentViewId() {
        return (Integer)getData();
    }
}

