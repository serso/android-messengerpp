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

package org.solovyev.android.messenger.sync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.accounts.Account;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 6:14 PM
 */
public interface SyncService {

	/**
	 * Method initializes service, must be called once before any other operations with current service
	 */
	void init();

	/**
	 * Method runs all synchronization tasks over all realms registered in system
	 *
	 * @param force true if all data should be synchronized regardless to individual synchronization parameters (frequency, scheduling, etc)
	 * @throws SyncAllTaskIsAlreadyRunning if task for synchronization is already running
	 */
	void syncAll(boolean force) throws SyncAllTaskIsAlreadyRunning;

	/**
	 * Method runs all synchronization tasks for specified <var>realm</var>
	 *
	 * @param force true if all data should be synchronized regardless to individual synchronization parameters (frequency, scheduling, etc)
	 * @throws SyncAllTaskIsAlreadyRunning if task for synchronization is already running
	 */
	void syncAllForAccount(@Nonnull Account account, boolean force) throws SyncAllTaskIsAlreadyRunning;

	void sync(@Nonnull SyncTask syncTask, @Nullable Runnable afterSyncCallback) throws TaskIsAlreadyRunningException;
}
