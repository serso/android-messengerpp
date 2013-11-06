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

public enum AccountState {
	enabled,

	/**
	 * Temporary state, indicates that user requested realm removal, but it cannot be done instantly => this states indicates that realm will be removed soon (e.g. on the next app boot)
	 */
	removed,

	/**
	 * Realm may be disable by app due to some error occurred in it (e.g. connection problems)
	 * NOTE: this state is reset every start up
	 */
	disabled_by_app,


	disabled_by_user;
}
