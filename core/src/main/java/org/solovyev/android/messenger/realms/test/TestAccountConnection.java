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

package org.solovyev.android.messenger.realms.test;

import android.content.Context;
import org.solovyev.android.messenger.accounts.connection.BaseAccountConnection;

import javax.annotation.Nonnull;

public class TestAccountConnection extends BaseAccountConnection<TestAccount> {

	public TestAccountConnection(@Nonnull TestAccount account, @Nonnull Context context) {
		super(account, context, false);
	}

	@Override
	protected void start0() {

	}

	@Override
	protected void stop0() {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
