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

/**
 * User: serso
 * Date: 3/9/13
 * Time: 2:45 PM
 */
public enum AccountEventType {

	/**
	 * Fired when account is created
	 */
	created,

	/**
	 * Fired when account is changed
	 */
	changed,

	/**
	 * Fired when only account configuration has been changed
	 */
	configuration_changed,

	/**
	 * Fired when account state is changed
	 */
	state_changed,

	/**
	 * Fired when account connection should be stopped for account
	 */
	stop,

	/**
	 * Fired when account connection should be started for account
	 */
	start;

	@Nonnull
	public AccountEvent newEvent(@Nonnull Account account, @Nullable Object data) {
		assert data == null;
		return new AccountEvent(account, this, null);
	}
}
