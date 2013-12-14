package org.solovyev.android.messenger.about;

import android.app.Activity;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.Identifiable;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;

public enum AboutType implements Identifiable {

	about(R.string.mpp_about, R.drawable.mpp_li_about_states) {
		@Nonnull
		@Override
		public MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack) {
			return MultiPaneFragmentDef.forClass(AboutFragment.FRAGMENT_TAG, addToBackStack, AboutFragment.class, activity, null);
		}
	},

	third_parties_licenses(R.string.mpp_about_third_party_licenses, R.drawable.mpp_li_3rd_parties_licenses_states) {
		@Nonnull
		@Override
		public MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack) {
			return MultiPaneFragmentDef.forClass(ThirdPartyLicensesFragment.FRAGMENT_TAG, addToBackStack, ThirdPartyLicensesFragment.class, activity, null);
		}
	};

	private final int titleResId;
	private final int iconResId;

	AboutType(int titleResId, int iconResId) {
		this.titleResId = titleResId;
		this.iconResId = iconResId;
	}

	public int getTitleResId() {
		return titleResId;
	}

	public int getIconResId() {
		return iconResId;
	}

	@Nonnull
	@Override
	public String getId() {
		return name();
	}

	@Nonnull
	public abstract MultiPaneFragmentDef newFragmentDef(@Nonnull Activity activity, boolean addToBackStack);
}
