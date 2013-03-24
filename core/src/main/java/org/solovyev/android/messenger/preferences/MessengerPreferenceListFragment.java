package org.solovyev.android.messenger.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 8:34 PM
 */
public final class MessengerPreferenceListFragment extends PreferenceListFragment {

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

    public MessengerPreferenceListFragment(int preferencesResId) {
        super(preferencesResId, R.layout.mpp_fragment_preferences, R.style.mpp_theme_metro_preferences);
    }

    public MessengerPreferenceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoboGuice.getInjector(this.getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    protected void onCreateView(@Nonnull Context context, @Nonnull LayoutInflater inflater, @Nonnull View root, @Nonnull ViewGroup container, @Nonnull Bundle b) {
        super.onCreateView(context, inflater, root, container, b);

        multiPaneManager.onCreatePane(getActivity(), container, root);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if ( preferenceScreen != null ) {
            final TextView fragmentTitle = (TextView) root.findViewById(R.id.mpp_fragment_title);
            if (fragmentTitle != null) {
                fragmentTitle.setText(preferenceScreen.getTitle());
            }
        }

        multiPaneManager.onPaneCreated(getActivity(), root);
    }
}
