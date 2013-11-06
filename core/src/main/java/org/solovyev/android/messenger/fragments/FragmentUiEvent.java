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

package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:18 PM
 */
public final class FragmentUiEvent extends AbstractTypedJEvent<Class<? extends Fragment>, FragmentUiEventType> {

	FragmentUiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentUiEventType type) {
		super(fragmentClass, type, View.NO_ID);
	}

	FragmentUiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentUiEventType type, int parentViewId) {
		super(fragmentClass, type, parentViewId);
	}

	@Nonnull
	public Class<? extends Fragment> getFragmentClass() {
		return getEventObject();
	}

	public int getParentViewId() {
		return (Integer) getData();
	}
}

