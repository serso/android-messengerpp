package org.solovyev.android.messenger;

import org.solovyev.android.messenger.core.R;

public enum MessengerTheme {

	holo(R.style.mpp_theme_holo, R.style.mpp_theme_holo_fragment),
	holo_light_action_bar(R.style.mpp_theme_holo_light, R.style.mpp_theme_holo_fragment);

	private final int themeResId;
	private final int contentThemeResId;

	MessengerTheme(int themeResId, int contentThemeResId) {
		this.themeResId = themeResId;
		this.contentThemeResId = contentThemeResId;
	}

	public int getThemeResId() {
		return themeResId;
	}

	public int getContentThemeResId() {
		return contentThemeResId;
	}
}
