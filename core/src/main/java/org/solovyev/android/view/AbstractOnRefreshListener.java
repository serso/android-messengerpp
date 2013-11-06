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

package org.solovyev.android.view;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class AbstractOnRefreshListener implements ListViewAwareOnRefreshListener, PullToRefreshListViewProvider {

	@Nullable
	private WeakReference<PullToRefreshListView> listViewRef;

	@Override
	public void setListView(@Nonnull PullToRefreshListView listView) {
		listViewRef = new WeakReference<PullToRefreshListView>(listView);
	}

	@Override
	@Nullable
	public PullToRefreshListView getPullToRefreshListView() {
		return listViewRef == null ? null : listViewRef.get();
	}

	public void completeRefresh() {
		final PullToRefreshListView lv = getPullToRefreshListView();
		if (lv != null) {
			lv.onRefreshComplete();
		}
	}

}
