package org.solovyev.android.messenger.about;

import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragment;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.solovyev.android.Resources.readRawResourceAsString;
import static org.solovyev.android.messenger.App.getApplication;


public class AboutFragment extends BaseFragment {

	public static final String FRAGMENT_TAG = "about";

	public AboutFragment() {
		super(R.layout.mpp_about, true);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final TextView aboutTextView = (TextView) root.findViewById(R.id.mpp_about_textview);
		try {
			aboutTextView.setText(readRawResourceAsString(R.raw.mpp_about, getResources(), new AboutFragmentLineProcessor(getApplication())));
			Linkify.addLinks(aboutTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		} catch (IOException e) {
			Log.e(App.TAG, e.getMessage(), e);
			aboutTextView.setText(null);
		}
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_about);
	}

}
