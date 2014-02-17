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
import android.support.v4.app.FragmentManager;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.EmptyFragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.contacts;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.messages;

public class MessengerMultiPaneFragmentManager extends MultiPaneFragmentManager {

	public static final List<PrimaryFragment> tabFragments;

	static {
		final List<PrimaryFragment> mutableTabFragments = new ArrayList<PrimaryFragment>();
		mutableTabFragments.add(contacts);
		mutableTabFragments.add(messages);
		tabFragments = unmodifiableList(mutableTabFragments);
	}

	public MessengerMultiPaneFragmentManager(@Nonnull BaseFragmentActivity activity) {
		super(activity, R.id.content_first_pane, EmptyFragment.class, EmptyFragment.FRAGMENT_TAG);
	}

	@Nonnull
	@Override
	public BaseFragmentActivity getActivity() {
		return (BaseFragmentActivity) super.getActivity();
	}

	public void setSecondFragment(@Nonnull MultiPaneFragmentDef fragmentDef) {
		setFragment(R.id.content_second_pane, fragmentDef);
	}

	public void emptifySecondFragment() {
		emptifyFragmentPane(R.id.content_second_pane);
	}

	public void setThirdFragment(@Nonnull MultiPaneFragmentDef fragmentDef) {
		setFragment(R.id.content_third_pane, fragmentDef);
	}

	public void emptifyThirdFragment() {
		emptifyFragmentPane(R.id.content_third_pane);
	}

	public void clearBackStack() {
		hideKeyboard();
		final FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Nullable
	public Fragment getFirstFragment() {
		final FragmentManager fm = getActivity().getSupportFragmentManager();
		return fm.findFragmentById(R.id.content_first_pane);
	}
}
