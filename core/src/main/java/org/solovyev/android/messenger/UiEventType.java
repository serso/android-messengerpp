package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

public enum UiEventType {
	show_realms,
	new_message,
	new_contact,
	app_exit;

	@Nonnull
	private UiEvent uiEvent;

	private UiEventType() {
		this.uiEvent = new UiEvent(this, null);
	}

	@Nonnull
	public UiEvent newEvent() {
		return uiEvent;
	}
}
