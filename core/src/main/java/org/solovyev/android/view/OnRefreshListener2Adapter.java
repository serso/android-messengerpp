package org.solovyev.android.view;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 4:03 PM
 */
public class OnRefreshListener2Adapter implements PullToRefreshBase.OnRefreshListener2 {

    @NotNull
    private PullToRefreshBase.OnRefreshListener onPullDownToRefresh;

    @NotNull
    private PullToRefreshBase.OnRefreshListener onPullUpToRefresh;

    public OnRefreshListener2Adapter(@NotNull PullToRefreshBase.OnRefreshListener onPullDownToRefresh,
                                     @NotNull PullToRefreshBase.OnRefreshListener onPullUpToRefresh) {
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
