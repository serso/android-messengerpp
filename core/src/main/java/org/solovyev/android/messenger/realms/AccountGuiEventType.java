package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:49 PM
 */
public enum AccountGuiEventType {

	/**
	 * Fired when account view is requested (e.g. account is clicked in the list of accounts)
	 */
	account_view_requested,

	/**
	 * Fired when account view is cancelled (e.g. user pressed 'Back' button)
	 */
	account_view_cancelled,

	/**
	 * Fired when editing of account is requested (e.g. 'Edit' button clicked)
	 */
	account_edit_requested,

	/**
	 * Fired when editing of account is finished (e.g. user pressed 'Back' or 'Save' button)
	 * Data; state (FinishedState)
	 */
	account_edit_finished;

	@Nonnull
	public static AccountGuiEvent newAccountViewRequestedEvent(@Nonnull Account account) {
		return new AccountGuiEvent(account, account_view_requested, null);
	}

	@Nonnull
	public static AccountGuiEvent newAccountViewCancelledEvent(@Nonnull Account account) {
		return new AccountGuiEvent(account, account_view_cancelled, null);
	}

	@Nonnull
	public static AccountGuiEvent newAccountEditRequestedEvent(@Nonnull Account account) {
		return new AccountGuiEvent(account, account_edit_requested, null);
	}

	@Nonnull
	public static AccountGuiEvent newAccountEditFinishedEvent(@Nonnull Account account, @Nonnull FinishedState state) {
		return new AccountGuiEvent(account, account_edit_finished, state);
	}

	public static enum FinishedState {
		back,
		removed,
		status_changed,
		saved;
	}
}
