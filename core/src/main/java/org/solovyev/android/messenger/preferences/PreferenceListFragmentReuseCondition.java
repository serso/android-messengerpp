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

import org.solovyev.android.fragments.AbstractFragmentReuseCondition;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 8:00 PM
 */
public final class PreferenceListFragmentReuseCondition extends AbstractFragmentReuseCondition<PreferenceListFragment> {

	private final int preferenceResId;

	private PreferenceListFragmentReuseCondition(int preferenceResId) {
		super(PreferenceListFragment.class);
		this.preferenceResId = preferenceResId;
	}

	@Nonnull
	public static PreferenceListFragmentReuseCondition newInstance(int preferenceResId) {
		return new PreferenceListFragmentReuseCondition(preferenceResId);
	}

	@Override
	protected boolean canReuseFragment(@Nonnull PreferenceListFragment fragment) {
		return fragment.getPreferencesResId() == preferenceResId;
	}
}
