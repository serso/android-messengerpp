package org.solovyev.android.messenger.about;

import android.app.Activity;

import javax.annotation.Nonnull;

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.core.R;

public enum AboutType implements Identifiable {

	about(R.string.mpp_about) {
		@Nonnull
		@Override
		public MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack) {
			return MultiPaneFragmentDef.forClass(AboutFragment.FRAGMENT_TAG, addToBackStack, AboutFragment.class, activity, null);
		}
	},

	third_parties_licenses(R.string.mpp_about_third_party_licenses) {
		@Nonnull
		@Override
		public MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack) {
			return MultiPaneFragmentDef.forClass(ThirdPartyLicensesFragment.FRAGMENT_TAG, addToBackStack, ThirdPartyLicensesFragment.class, activity, null);
		}
	};

	private final int titleResId;

	AboutType(int titleResId) {
		this.titleResId = titleResId;
	}

	public int getTitleResId() {
		return titleResId;
	}

	@Nonnull
	@Override
	public String getId() {
		return name();
	}

	@Nonnull
	public abstract MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack);
}
