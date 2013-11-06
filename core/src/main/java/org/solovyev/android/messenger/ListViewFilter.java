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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:14 PM
 */
public class ListViewFilter {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	@Nonnull
	private static final String FILTER = "filter";

	@Nonnull
	private static final String TAG = "ListViewFilter";

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final ListFragment fragment;

	@Nonnull
	private final FilterableListView filterableListView;

	/**
	 * <var>filterEditText</var> might be null if view has not been created yet (i.e. {@link ListViewFilter#createView(android.os.Bundle)} method has not been called )
	 */
	private EditText filterEditText;

	public ListViewFilter(@Nonnull ListFragment fragment, @Nonnull FilterableListView filterableListView) {
		this.fragment = fragment;
		this.filterableListView = filterableListView;
	}

	@Nonnull
	public View createView(@Nullable Bundle savedInstanceState) {
		final FragmentActivity activity = fragment.getActivity();
		if (activity != null) {
			final ViewGroup result = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.mpp_list_filter).build(activity);

			filterEditText = (EditText) result.findViewById(R.id.mpp_filter_edittext);
			if (savedInstanceState != null) {
				final String filter = savedInstanceState.getString(FILTER);
				filterEditText.setText(filter);
			}
			filterEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					/**
					 *  Fragment's {@link android.support.v4.app.Fragment#restoreViewState()} is called after {@link android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)} =>
					 *  => we need to update view visibility according to restored values
					 */
					if (!Strings.isEmpty(s)) {
						setFilterBoxVisible(true);
					}
					filterableListView.filter(s);
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			return result;
		} else {
			throw new IllegalStateException("Activity must be attached to fragment before creating filter!");
		}
	}

	public void onViewCreated() {
		if (Strings.isEmpty(getFilterText())) {
			setFilterBoxVisible(false);
		} else {
			setFilterBoxVisible(true);
		}
	}

	public void toggleView() {
		final View view = fragment.getView();
		final FragmentActivity activity = fragment.getActivity();

		if (view != null && activity != null && filterEditText != null) {
			final ViewGroup filterBox = (ViewGroup) view.findViewById(R.id.mpp_list_filter);
			if (filterBox != null) {
				int visibility = filterBox.getVisibility();

				final InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

				if (visibility != View.VISIBLE) {
					filterBox.setVisibility(View.VISIBLE);
					filterEditText.requestFocus();

					manager.showSoftInput(filterEditText, InputMethodManager.SHOW_IMPLICIT);

				} else if (visibility != View.GONE) {
					// if filter box is visible before hiding it clear filter query
					filterEditText.getText().clear();
					filterEditText.clearFocus();

					manager.hideSoftInputFromWindow(filterEditText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

					filterBox.setVisibility(View.GONE);
				}
			}
		} else {
			Log.e(TAG, "toggleView() is called when view or activity is detached from fragment!");
		}
	}

	public void setFilterBoxVisible(boolean visible) {
		final View view = this.fragment.getView();
		if (view != null && filterEditText != null) {
			final ViewGroup filterBox = (ViewGroup) view.findViewById(R.id.mpp_list_filter);
			if (filterBox != null) {
				setFilterBoxVisible(visible, filterBox);
			}
		} else {
			Log.e(TAG, "setFilterBoxVisible(boolean) is called when view is detached from fragment!");
		}
	}

	private void setFilterBoxVisible(boolean visible, @Nonnull ViewGroup filterBox) {
		if (visible) {
			filterBox.setVisibility(View.VISIBLE);
		} else {
			filterBox.setVisibility(View.GONE);
		}
	}

	public void saveState(Bundle outState) {
		if (filterEditText != null) {
			outState.putString(FILTER, filterEditText.getText().toString());
		}
	}

	@Nullable
	public CharSequence getFilterText() {
		if (filterEditText != null) {
			return filterEditText.getText();
		} else {
			return null;
		}
	}

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

	public static interface FilterableListView {

		/**
		 * Method called when text in filter box is changed
		 *
		 * @param filterText new value of text in filter box (=> list view must filtered with this text)
		 */
		void filter(@Nullable CharSequence filterText);
	}
}
