package org.solovyev.android.messenger;

import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;

public enum MessengerTheme {

	holo(R.string.mpp_preferences_theme_holo,
			R.style.mpp_theme_holo,
			R.style.mpp_theme_holo_fragment,
			R.style.mpp_theme_holo_dialog,
			R.drawable.mpp_app_icon) {
		@Nonnull
		@Override
		public Icons getIcons(boolean dialog) {
			return dialog ? Icons.dark : Icons.light;
		}
	},

	holo_light_action_bar(R.string.mpp_preferences_theme_holo_light_action_bar,
			R.style.mpp_theme_holo_light,
			R.style.mpp_theme_holo_fragment,
			R.style.mpp_theme_holo_light_dialog,
			R.drawable.mpp_app_icon_blue) {
		@Nonnull
		@Override
		public Icons getIcons(boolean dialog) {
			return Icons.light;
		}
	};

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

	@Nonnull
	public abstract Icons getIcons(boolean dialog);

	public static final class Icons {

		@Nonnull
		private static final Icons light = new Icons(R.drawable.mpp_ab_edit_light, R.drawable.mpp_ab_remove_light);

		@Nonnull
		private static final Icons dark = new Icons(R.drawable.mpp_ab_edit, R.drawable.mpp_ab_remove);

		public final int edit;
		public final int remove;

		private Icons(int edit, int remove) {
			this.edit = edit;
			this.remove = remove;
		}
	}
}
