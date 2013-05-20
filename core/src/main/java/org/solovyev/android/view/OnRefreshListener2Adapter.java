package org.solovyev.android.view;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 4:03 PM
 */
public class OnRefreshListener2Adapter implements PullToRefreshBase.OnRefreshListener2 {

	@Nonnull
	private PullToRefreshBase.OnRefreshListener onPullDownToRefresh;

	@Nonnull
	private PullToRefreshBase.OnRefreshListener onPullUpToRefresh;

	public OnRefreshListener2Adapter(@Nonnull PullToRefreshBase.OnRefreshListener onPullDownToRefresh,
									 @Nonnull PullToRefreshBase.OnRefreshListener onPullUpToRefresh) {
		this.onPullDownToRefresh = onPullDownToRefresh;
		this.onPullUpToRefresh = onPullUpToRefresh;
	}

	@Override
	public void onPullDownToRefresh() {
		onPullDownToRefresh.onRefresh();
	}

	@Override
	public void onPullUpToRefresh() {
		onPullUpToRefresh.onRefresh();
	}
}
