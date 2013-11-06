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

package org.solovyev.android.messenger.longpoll;

import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.AccountException;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:09 AM
 */
public interface RealmLongPollService {

	@Nullable
	Object startLongPolling() throws AccountException;


	@Nullable
	LongPollResult waitForResult(@Nullable Object longPollingData) throws AccountException;
}
