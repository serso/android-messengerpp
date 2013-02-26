package org.solovyev.android.messenger.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import org.solovyev.android.messenger.MessengerApplication;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 2:13 AM
 */
public class MessengerEmptyFragment extends RoboSherlockFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout root = new LinearLayout(this.getActivity());

        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        MessengerApplication.getMultiPaneManager().fillContentPane(this.getActivity(), container, root);

        return root;
    }
}
