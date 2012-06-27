package org.solovyev.android.view;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.jetbrains.annotations.NotNull;

/**
* User: serso
* Date: 6/8/12
* Time: 4:14 PM
*/
public interface ListViewAwareOnRefreshListener extends PullToRefreshBase.OnRefreshListener {
    void setListView(@NotNull PullToRefreshListView listView);
}
