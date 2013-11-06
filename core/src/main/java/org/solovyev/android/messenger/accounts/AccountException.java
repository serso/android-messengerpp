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

public class AccountException extends Exception {

	@Nonnull
	private final String accountId;

	public AccountException(@Nonnull String accountId) {
		this.accountId = accountId;
	}

	public AccountException(@Nonnull String accountId, @Nonnull Throwable throwable) {
		super(unwrap(throwable));
		this.accountId = accountId;
	}

	public AccountException(@Nonnull AccountRuntimeException exception) {
		super(unwrap(exception));
		this.accountId = exception.getAccountId();
	}

	@Nonnull
	private static Throwable unwrap(@Nonnull Throwable exception) {
		if (exception instanceof AccountRuntimeException) {
			final Throwable cause = exception.getCause();
			return cause != null ? cause : exception;
		} else {
			return exception;
		}
	}

	@Nonnull
	public final String getAccountId() {
		return accountId;
	}
}
