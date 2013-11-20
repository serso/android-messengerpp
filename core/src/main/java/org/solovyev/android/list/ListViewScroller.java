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

package org.solovyev.android.list;

import android.app.ListActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import org.solovyev.android.messenger.App;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public final class ListViewScroller implements AbsListView.OnScrollListener {

	private static final String TAG = App.newTag("ListScroller");

	/**
	 * First visible item in list view. The value is changed due to list view scroll changes
	 */
	@Nonnull
	private final AtomicInteger firstVisibleItem = new AtomicInteger(-1);

	@Nonnull
	private final ListViewScrollerListener listener;

	private ListViewScroller(@Nonnull ListViewScrollerListener listener) {
		this.listener = listener;
	}

	public static <A extends ListActivity & ListViewScrollerListener> void createAndAttach(@Nonnull A activity) {
		activity.getListView().setOnScrollListener(new ListViewScroller(activity));
	}

	public static <F extends ListFragment & ListViewScrollerListener> void createAndAttach(@Nonnull F fragment) {
		fragment.getListView().setOnScrollListener(new ListViewScroller(fragment));
	}

	public static void createAndAttach(@Nonnull ListView listView, @Nonnull ListViewScrollerListener listener) {
		listView.setOnScrollListener(new ListViewScroller(listener));
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
				view.setScrollingCacheEnabled(false);
				break;
			case SCROLL_STATE_IDLE:
				view.setScrollingCacheEnabled(true);
				break;
		}
	}

	@Override
	public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// we want to notify subclasses about several events
		// 1. onTopReached
		// 2. onBottomReached
		// 3. onItemReachedFromTop
		// 4. onItemReachedFromBottom

		if (this.firstVisibleItem.get() >= 0 && visibleItemCount > 0) {
			boolean scrollUp = false;
			boolean scrollDown = false;
			if (firstVisibleItem < this.firstVisibleItem.get()) {
				scrollUp = true;
			}
			if (firstVisibleItem > this.firstVisibleItem.get()) {
				scrollDown = true;
			}

			final int lastVisibleItem = firstVisibleItem + visibleItemCount;

			switch (view.getId()) {
				case android.R.id.list:
					if (scrollUp && firstVisibleItem == 0) {
						Log.d(TAG, "Top reached");
						// reach top
						listener.onTopReached();
					} else {
						if (scrollDown && lastVisibleItem == totalItemCount) {
							// reach bottom
							Log.d(TAG, "Bottom reached");
							listener.onBottomReached();
						}
					}

					if (scrollDown) {
						listener.onItemReachedFromTop(lastVisibleItem, totalItemCount);
					}

					if (scrollUp) {
						listener.onItemReachedFromBottom(lastVisibleItem, totalItemCount);
					}

					break;
			}
		}

		this.firstVisibleItem.set(firstVisibleItem);
	}
}
