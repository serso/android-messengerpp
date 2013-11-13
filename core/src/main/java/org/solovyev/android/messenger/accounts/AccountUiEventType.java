/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum AccountUiEventType {

	/**
	 * Fired when account view is requested (e.g. account is clicked in the list of accounts)
	 */
	account_view_requested,
	account_picked,

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
	account_edit_finished {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof FinishedState;
		}
	};

	@Nonnull
	public AccountUiEvent newEvent(@Nonnull Account account) {
		return newEvent(account, null);
	}

	@Nonnull
	public AccountUiEvent newEvent(@Nonnull Account account, @Nullable Object data) {
		checkData(data);
		return new AccountUiEvent(account, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}

	public static enum FinishedState {
		back,
		removed,
		status_changed,
		saved
	}
}
