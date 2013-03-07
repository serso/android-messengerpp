package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:16 PM
 */
public enum FragmentGuiEventType {

    /**
     * After {@link android.app.Fragment#onCreate(android.os.Bundle)} is called
     */
    created,

    /**
     * After {@link android.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)} is called
     */
    shown,

    started;

    @Nonnull
    public static FragmentGuiEvent newFragmentCreatedEvent(@Nonnull Fragment fragment) {
        return new FragmentGuiEvent(created, fragment.getClass());
    }

    @Nonnull
    public static FragmentGuiEvent newFragmentShownEvent(@Nonnull Fragment fragment) {
        return newFragmentEvent(fragment, shown);
    }

    private static FragmentGuiEvent newFragmentEvent(@Nonnull Fragment fragment, @Nonnull FragmentGuiEventType type) {
        final View view = fragment.getView();
        if ( view == null ) {
            throw new IllegalArgumentException("View is not created for fragment, but fragment is shown!");
        }

        if ( view.getParent() instanceof View ) {
            return new FragmentGuiEvent(type, fragment.getClass(), ((View) view.getParent()).getId());
        } else {
            return new FragmentGuiEvent(type, fragment.getClass());
        }
    }

    @Nonnull
    public static FragmentGuiEvent newFragmentStartedEvent(@Nonnull Fragment fragment) {
        return newFragmentEvent(fragment, started);
    }
}
