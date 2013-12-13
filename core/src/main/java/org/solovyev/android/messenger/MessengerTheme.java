package org.solovyev.android.messenger;

import org.solovyev.android.messenger.core.R;

public enum MessengerTheme {

	holo(R.string.mpp_preferences_theme_holo, R.style.mpp_theme_holo, R.style.mpp_theme_holo_fragment, R.style.mpp_theme_holo_dialog),
	holo_light_action_bar(R.string.mpp_preferences_theme_holo_light_action_bar, R.style.mpp_theme_holo_light, R.style.mpp_theme_holo_fragment, R.style.mpp_theme_holo_light_dialog);

	private final int nameResId;
	private final int themeResId;
	private final int contentThemeResId;
	private final int dialogThemeResId;

	MessengerTheme(int nameResId, int themeResId, int contentThemeResId, int dialogThemeResId) {
		this.nameResId = nameResId;
		this.themeResId = themeResId;
		this.contentThemeResId = contentThemeResId;
		this.dialogThemeResId = dialogThemeResId;
	}

	public int getNameResId() {
		return nameResId;
	}

	public int getThemeResId() {
		return themeResId;
	}

	public int getContentThemeResId() {
		return contentThemeResId;
	}

	public int getDialogThemeResId() {
		return dialogThemeResId;
	}
}
