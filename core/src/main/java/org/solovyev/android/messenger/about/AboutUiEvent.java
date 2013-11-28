package org.solovyev.android.messenger.about;

import javax.annotation.Nonnull;

interface AboutUiEvent {

	public static class Clicked implements AboutUiEvent {

		@Nonnull
		private final AboutType type;

		Clicked(@Nonnull AboutType type) {
			this.type = type;
		}

		@Nonnull
		public AboutType getType() {
			return type;
		}
	}
}
