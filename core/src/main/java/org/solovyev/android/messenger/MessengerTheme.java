package org.solovyev.android.messenger;

import org.solovyev.android.messenger.core.R;

public enum MessengerTheme {

	holo(R.string.mpp_preferences_theme_holo, R.style.mpp_theme_holo, R.style.mpp_theme_holo_fragment, R.style.mpp_theme_holo_dialog, R.drawable.mpp_app_icon),
	holo_light_action_bar(R.string.mpp_preferences_theme_holo_light_action_bar, R.style.mpp_theme_holo_light, R.style.mpp_theme_holo_fragment, R.style.mpp_theme_holo_light_dialog, R.drawable.mpp_app_icon_blue);

	private final int nameResId;
	private final int themeResId;
	private final int contentThemeResId;
	private final int dialogThemeResId;
	private final int actionBarIconResId;

	MessengerTheme(int nameResId, int themeResId, int contentThemeResId, int dialogThemeResId, int actionBarIconResId) {
		this.nameResId = nameResId;
		this.themeResId = themeResId;
		this.contentThemeResId = contentThemeResId;
		this.dialogThemeResId = dialogThemeResId;
		this.actionBarIconResId = actionBarIconResId;
	}

	public int getNameResId() {
		return nameResId;
	}

	public int getThemeResId() {
		return themeResId;
	}

	public int getContentThemeResId(boolean dialog) {
		return dialog ? dialogThemeResId : contentThemeResId;
	}

	public int getDialogThemeResId() {
		return dialogThemeResId;
	}

	public int getActionBarIconResId() {
		return actionBarIconResId;
	}
}
