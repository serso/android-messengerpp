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

package org.solovyev.android.messenger.view;

import android.support.v4.app.Fragment;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.messenger.BaseListFragment;
import org.solovyev.common.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FragmentMenu implements
		ActionBarSherlock.OnCreateOptionsMenuListener,
		ActionBarSherlock.OnOptionsItemSelectedListener,
		ActionBarSherlock.OnPrepareOptionsMenuListener {

	@Nonnull
	private final Fragment fragment;

	@Nonnull
	private final Builder<ActivityMenu<Menu, MenuItem>> menuBuilder;

	@Nullable
	private ActivityMenu<Menu, MenuItem> menu;

	public FragmentMenu(@Nonnull Fragment fragment, @Nonnull Builder<ActivityMenu<Menu, MenuItem>> menuBuilder) {
		this.fragment = fragment;
		this.menuBuilder = menuBuilder;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (shouldShowMenu()) {
			this.menu = menuBuilder.build();
			return this.menu.onCreateOptionsMenu(fragment.getActivity(), menu);
		} else {
			this.menu = null;
			return false;
		}
	}

	private boolean shouldShowMenu() {
		if (fragment instanceof BaseListFragment && !((BaseListFragment) fragment).wasViewCreated()) {
			// view is not created but it requests menu => show it
			return true;
		}

		// show menu if fragment is visible
		return fragment.isVisible();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return shouldShowMenu() && this.menu != null && this.menu.onPrepareOptionsMenu(fragment.getActivity(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return shouldShowMenu() && this.menu != null && menu.onOptionsItemSelected(fragment.getActivity(), item);
	}
}
