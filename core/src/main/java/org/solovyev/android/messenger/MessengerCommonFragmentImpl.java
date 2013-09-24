package org.solovyev.android.messenger;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.Button;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 2:11 PM
 */
public class MessengerCommonFragmentImpl implements MessengerCommonFragment {
	@Nonnull
	@Override
	public Button createFooterButton(int captionResId, @Nonnull Activity activity) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Nonnull
	@Override
	public ViewGroup getFooter(@Nonnull Fragment fragment) {
		return (ViewGroup) fragment.getView().findViewById(R.id.footer);
	}
}
