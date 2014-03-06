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

package org.solovyev.android.messenger.realms;

import org.solovyev.common.listeners.JEvent;

import javax.annotation.Nonnull;

public abstract class RealmUiEvent implements JEvent {

	@Nonnull
	public final Realm realm;

	public RealmUiEvent(@Nonnull Realm realm) {
		this.realm = realm;
	}

	public static final class Clicked extends RealmUiEvent {
		public Clicked(@Nonnull Realm realm) {
			super(realm);
		}
	}
}
