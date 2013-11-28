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

package org.solovyev.android.messenger.preferences;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.Identifiable;

public final class PreferenceGroup implements Identifiable {

	private static final int NO_ICON = -1;

	@Nonnull
	private final String id;

	private final int nameResId;

	private final int preferencesResId;

	private final int iconResId;

	public PreferenceGroup(@Nonnull String id, int nameResId, int preferencesResId) {
		this.id = id;
		this.nameResId = nameResId;
		this.preferencesResId = preferencesResId;
		this.iconResId = NO_ICON;
	}

	public PreferenceGroup(@Nonnull String id, int nameResId, int preferencesResId, int iconResId) {
		this.id = id;
		this.nameResId = nameResId;
		this.preferencesResId = preferencesResId;
		this.iconResId = iconResId;
	}

	@Nonnull
	@Override
	public String getId() {
		return this.id;
	}

	public int getNameResId() {
		return nameResId;
	}

	public int getPreferencesResId() {
		return preferencesResId;
	}

	public boolean hasIcon() {
		return iconResId != NO_ICON;
	}

	public int getIconResId() {
		return iconResId;
	}
}
