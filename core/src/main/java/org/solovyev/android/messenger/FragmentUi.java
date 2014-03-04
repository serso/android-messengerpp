/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FragmentUi {

	@Nonnull
	private final Fragment fragment;

	/**
	 * Last saved instance state (the last state which has been passed through onCreate method)
	 * <p/>
	 * Problem: When fragment is in back stack and has not been shown during activity lifecycle then
	 * onSaveInstanceState() saves nothing on activity destruction (as no view has been created). If later we return to this fragment using back button we loose state.
	 * <p/>
	 * Solution: Save last state and if fragment hsa not been shown - use it.
	 */
	@Nullable
	private Bundle lastSavedInstanceState;

	private boolean viewWasCreated = false;

	public FragmentUi(@Nonnull Fragment fragment) {
		this.fragment = fragment;
	}

	@Nonnull
	public Fragment getFragment() {
		return fragment;
	}

	public boolean wasViewCreated() {
		return viewWasCreated;
	}

	/*
	**********************************************************************
	*
	*                           LIFECYCLE
	*
	**********************************************************************
	*/

	public void onCreate(@Nullable Bundle savedInstanceState) {
		lastSavedInstanceState = savedInstanceState;
		viewWasCreated = false;
	}

	public void onViewCreated() {
		viewWasCreated = true;
	}

	public void copyLastSavedInstanceState(@Nonnull Bundle outState) {
		if (lastSavedInstanceState != null) {
			outState.putAll(lastSavedInstanceState);
		}
	}

	public void clearLastSavedInstanceState() {
		lastSavedInstanceState = null;
	}

	public boolean isExistsLastSavedInstanceState() {
		return lastSavedInstanceState != null;
	}
}
