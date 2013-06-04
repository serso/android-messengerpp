package org.solovyev.android.list;

import android.app.ListActivity;
import android.support.v4.app.ListFragment;
import android.widget.AbsListView;
import android.widget.ListView;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 6/4/13
 * Time: 7:55 PM
 */
public final class ListViewScroller implements AbsListView.OnScrollListener {

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
		// do nothing
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
						// reach top
						listener.onTopReached();
					} else {
						if (scrollDown && lastVisibleItem == totalItemCount) {
							// reach bottom
							listener.onBottomReached();
						}
					}

					if (scrollDown) {
						listener.onItemReachedFromTop(lastVisibleItem);
					}

					if (scrollUp) {
						listener.onItemReachedFromBottom(lastVisibleItem);
					}

					break;
			}
		}

		this.firstVisibleItem.set(firstVisibleItem);
	}
}
