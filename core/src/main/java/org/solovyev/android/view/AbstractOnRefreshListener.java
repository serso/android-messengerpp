package org.solovyev.android.view;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * User: serso
 * Date: 6/8/12
 * Time: 4:14 PM
 */
public abstract class AbstractOnRefreshListener implements ListViewAwareOnRefreshListener, PullToRefreshListViewProvider {

    @Nullable
    private WeakReference<PullToRefreshListView> listViewRef;

    @Override
    public void setListView(@NotNull PullToRefreshListView listView) {
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
