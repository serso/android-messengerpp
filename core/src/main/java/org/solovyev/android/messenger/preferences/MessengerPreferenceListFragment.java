package org.solovyev.android.messenger.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 8:34 PM
 */
public class MessengerPreferenceListFragment extends PreferenceListFragment {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED VIEWS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private MessengerMultiPaneManager multiPaneManager;

    public MessengerPreferenceListFragment(int preferencesResId, int layoutResId) {
        super(preferencesResId, layoutResId);
    }

    public MessengerPreferenceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoboGuice.getInjector(this.getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b) {
        final View root = super.onCreateView(inflater, container, b);
        multiPaneManager.fillContentPane(getActivity(), container, root);
        return root;
    }
}
