package org.solovyev.android.view;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/25/12
 * Time: 1:35 AM
 */
public interface PullToRefreshListViewProvider {

	@Nullable
	PullToRefreshListView getPullToRefreshListView();
}
