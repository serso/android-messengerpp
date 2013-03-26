package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;

public interface FragmentDef {

    @Nonnull
    String getFragmentTag();

    @Nonnull
    Class<? extends Fragment> getFragmentClass();

    boolean isAddToBackStack();
}
