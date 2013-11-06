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

package org.solovyev.android.messenger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.core.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class EditButtons<F extends Fragment> {

	@Nonnull
	private final F fragment;

	@Nonnull
	private Button backButton;

	@Nonnull
	private Button saveButton;

	@Nonnull
	private Button removeButton;

	public EditButtons(@Nonnull F fragment) {
		this.fragment = fragment;
	}

	public void onViewCreated(View root, Bundle savedInstanceState) {
		backButton = (Button) root.findViewById(R.id.mpp_back_button);

		if (isBackButtonVisible()) {
			backButton.setVisibility(VISIBLE);
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackButtonPressed();
				}
			});
		} else {
			backButton.setVisibility(GONE);
		}

		removeButton = (Button) root.findViewById(R.id.mpp_remove_button);
		if (isRemoveButtonVisible()) {
			removeButton.setVisibility(VISIBLE);
			removeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onRemoveButtonPressed();
				}
			});
		} else {
			removeButton.setVisibility(GONE);
		}


		saveButton = (Button) root.findViewById(R.id.mpp_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveButtonPressed();
			}
		});
	}

	@Nonnull
	protected BaseFragmentActivity getActivity() {
		return (BaseFragmentActivity) fragment.getActivity();
	}

	@Nonnull
	protected F getFragment() {
		return fragment;
	}

	protected abstract boolean isRemoveButtonVisible();

	protected abstract void onRemoveButtonPressed();

	protected abstract boolean isBackButtonVisible();

	protected abstract void onSaveButtonPressed();

	protected abstract void onBackButtonPressed();
}
