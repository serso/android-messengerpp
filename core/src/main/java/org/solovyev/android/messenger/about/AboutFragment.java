package org.solovyev.android.messenger.about;

import javax.annotation.Nullable;

import org.solovyev.android.messenger.BaseFragment;
import org.solovyev.android.messenger.core.R;


public class AboutFragment extends BaseFragment {

	public static final String FRAGMENT_TAG = "about";

	public AboutFragment() {
		super(R.layout.mpp_about, true);
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_about);
	}
}
