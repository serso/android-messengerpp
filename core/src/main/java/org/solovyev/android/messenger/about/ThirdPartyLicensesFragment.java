package org.solovyev.android.messenger.about;

import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.solovyev.android.Resources;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.BaseFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.solovyev.android.Resources.readRawResourceAsString;

public class ThirdPartyLicensesFragment extends BaseFragment {

	public static final String FRAGMENT_TAG = "third-party-licenses";

	public ThirdPartyLicensesFragment() {
		super(R.layout.mpp_about);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final TextView aboutTextView = (TextView) root.findViewById(R.id.mpp_about_textview);
		try {
			aboutTextView.setText(readRawResourceAsString(R.raw.mpp_licenses, getResources(), new LicensesLineProcessor()));
			Linkify.addLinks(aboutTextView, Linkify.WEB_URLS);
		} catch (IOException e) {
			Log.e(App.TAG, e.getMessage(), e);
			aboutTextView.setText(null);
		}
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getString(R.string.mpp_about_third_party_licenses);
	}

	private static class LicensesLineProcessor implements Resources.LineProcessor {
		@Nonnull
		@Override
		public String process(@Nonnull String line) {
			return line + Strings.LINE_SEPARATOR;
		}
	}
}
