package org.solovyev.android.messenger;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.OnRefreshListener2Adapter;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:57 PM
 */
public abstract class AbstractMessengerListFragment<T> extends ListFragment implements AbsListView.OnScrollListener {

    @Nullable
    private UserEventListener userEventListener;

    @NotNull
    private User user;

    @NotNull
    private AbstractMessengerListItemAdapter adapter;

    @Nullable
    private MessengerAsyncTask<Void, Void, List<T>> listLoader;

    @Nullable
    private EditText filterInput;

    @NotNull
    private final String tag;

    @Nullable
    private PullToRefreshListView2 pullLv;

    public AbstractMessengerListFragment(@NotNull String tag) {
        this.tag = tag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate: " + this);

        try {
            this.user = getServiceLocator().getAuthServiceFacade().getUser(getActivity());
        } catch (UserIsNotLoggedInException e) {
            MessengerLoginActivity.startActivity(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "onCreateView: " + this);

        final LinearLayout root = new LinearLayout(this.getActivity());
        root.setOrientation(LinearLayout.VERTICAL);

        if (isFilterEnabled()) {
            final View searchBoxLayout = ViewFromLayoutBuilder.newInstance(R.layout.msg_list_filter).build(this.getActivity());
            root.addView(searchBoxLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            filterInput = (EditText) searchBoxLayout.findViewById(R.id.filter_box);
            filterInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterTextChanged(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        final View listViewParent = createListView(inflater, container);
        final ListView listView = getListView(listViewParent);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        root.addView(listViewParent, params);

        return root;
    }

    @NotNull
    protected ListView getListView(@NotNull View root) {
        return (ListView) root.findViewById(android.R.id.list);
    }

    protected abstract boolean isFilterEnabled();

    protected AbstractMessengerListItemAdapter getAdapter() {
        return adapter;
    }

    public static class PullToRefreshListView2 extends PullToRefreshListView {

        public PullToRefreshListView2(Context context) {
            super(context);
        }

        public PullToRefreshListView2(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public PullToRefreshListView2(Context context, Mode mode) {
            super(context, mode);
        }

        @Override
        public void setRefreshingInternal(boolean doScroll) {
            super.setRefreshingInternal(doScroll);
        }
    }

    /*
    COPIED FROM LIST FRAGMENT
    */

    private static final int INTERNAL_EMPTY_ID = 0x00ff0001;
    private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    private View createListView(@NotNull LayoutInflater inflater, ViewGroup container) {
        final Context context = getActivity();

        FrameLayout root = new FrameLayout(context);

        // ------------------------------------------------------------------

        LinearLayout pframe = new LinearLayout(context);
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        pframe.setOrientation(LinearLayout.VERTICAL);
        pframe.setVisibility(View.GONE);
        pframe.setGravity(Gravity.CENTER);

        ProgressBar progress = new ProgressBar(context, null,
                android.R.attr.progressBarStyleLarge);
        pframe.addView(progress, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(pframe, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        // ------------------------------------------------------------------

        FrameLayout lframe = new FrameLayout(context);
        lframe.setId(INTERNAL_LIST_CONTAINER_ID);

        TextView tv = new TextView(context);
        tv.setId(INTERNAL_EMPTY_ID);
        tv.setGravity(Gravity.CENTER);
        lframe.addView(tv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


        final ListViewAwareOnRefreshListener topRefreshListener = getTopPullRefreshListener();
        final ListViewAwareOnRefreshListener bottomRefreshListener = getBottomPullRefreshListener();

        final View lv;


        final Resources resources = context.getResources();
        if (topRefreshListener == null && bottomRefreshListener == null) {
            lv = new ListView(context);
            fillListView((ListView) lv, context);
            lv.setId(android.R.id.list);
        } else if (topRefreshListener != null && bottomRefreshListener != null) {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.BOTH);

            topRefreshListener.setListView(pullLv);
            bottomRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);
            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView());
            prepareLoadingView(resources, pullLv.getFooterLoadingView());

            pullLv.setOnRefreshListener(new OnRefreshListener2Adapter(topRefreshListener, bottomRefreshListener));
            lv = pullLv;
        } else if (topRefreshListener != null) {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);

            topRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);

            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView());
            prepareLoadingView(resources, pullLv.getFooterLoadingView());

            pullLv.setOnRefreshListener(topRefreshListener);

            lv = pullLv;
        } else {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);

            bottomRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);
            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView());
            prepareLoadingView(resources, pullLv.getFooterLoadingView());

            pullLv.setOnRefreshListener(bottomRefreshListener);

            lv = pullLv;
        }

        lframe.addView(lv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        root.addView(lframe, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        // ------------------------------------------------------------------

        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        return root;
    }

    @Nullable
    protected PullToRefreshListView2 getPullLv() {
        return pullLv;
    }

    protected void fillListView(@NotNull ListView lv, @NotNull Context context) {
        lv.setScrollbarFadingEnabled(true);
        lv.setOnScrollListener(this);
        lv.setDividerHeight(2);
    }

    private void prepareLoadingView(@NotNull Resources resources, @Nullable LoadingLayout loadingView) {
        if (loadingView != null) {
            loadingView.setTextColor(resources.getColor(R.color.text));
            loadingView.setBackgroundColor(resources.getColor(R.color.base_bg));
        }
    }

    @Nullable
    protected abstract ListViewAwareOnRefreshListener getTopPullRefreshListener();

    @Nullable
    protected abstract ListViewAwareOnRefreshListener getBottomPullRefreshListener();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(tag, "onActivityCreated: " + this);

        adapter = createAdapter();

        userEventListener = new UiThreadUserEventListener();
        getServiceLocator().getUserService().addUserEventListener(userEventListener);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setAdapter(adapter);
        lv.setVerticalFadingEdgeEnabled(false);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent,
                                    final View view,
                                    final int position,
                                    final long id) {
                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                final ListItem.OnClickAction onClickAction = listItem.getOnClickAction();
                if (onClickAction != null) {
                    onClickAction.onClick(getActivity(), adapter, getListView());
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                final ListItem.OnClickAction onLongClickAction = listItem.getOnLongClickAction();
                if (onLongClickAction != null) {
                    onLongClickAction.onClick(getActivity(), adapter, getListView());
                    return true;
                }

                return false;
            }
        });

        listLoader = createAsyncLoader(adapter, new Runnable() {
            @Override
            public void run() {
                try {
                    // change adapter state
                    adapter.setInitialized(true);

                    // change UI state
                    setListShown(true);

                    // apply filter if any
                    if (filterInput != null) {
                        filterTextChanged(filterInput.getText());
                    } else {
                        filterTextChanged("");
                    }
                } catch (IllegalStateException e) {
                    // todo serso: find the reason of the exception
                    Log.e(tag, e.getMessage(), e);
                }
            }
        });
        listLoader.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listLoader != null) {
            listLoader.cancel(false);
        }

        if (userEventListener != null) {
            getServiceLocator().getUserService().removeUserEventListener(userEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void filterTextChanged(@NotNull CharSequence searchText) {
        if (this.adapter != null) {
            if (this.adapter.isInitialized()) {
                this.adapter.filter(searchText);
            }
        }
    }


    @NotNull
    protected User getUser() {
        return this.user;
    }

    @NotNull
    public static ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @NotNull
    protected abstract AbstractMessengerListItemAdapter createAdapter();

    @NotNull
    protected abstract MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@NotNull AbstractMessengerListItemAdapter adapter, @NotNull Runnable onPostExecute);

    @Override
    public final void onScrollStateChanged(AbsListView view, int scrollState) {
        // do nothing
    }

    @NotNull
    private final AtomicInteger firstVisibleItem = new AtomicInteger(-1);

    @Override
    public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.firstVisibleItem.get() >= 0 && visibleItemCount > 0) {
            boolean scrollUp = false;
            boolean scrollDown = false;
            if ( firstVisibleItem < this.firstVisibleItem.get() ) {
                scrollUp = true;
            }
            if ( firstVisibleItem > this.firstVisibleItem.get() ) {
                scrollDown = true;
            }

            final int lastVisibleItem = firstVisibleItem + visibleItemCount;

            switch (view.getId()) {
                case android.R.id.list:
                    if (scrollUp && firstVisibleItem == 0) {
                        // reach top
                        onListViewTopReached();
                    } else {
                        if (scrollDown && lastVisibleItem == totalItemCount) {
                            // reach bottom
                            onListViewBottomReached();
                        }
                    }

                    if ( scrollDown ) {
                        onItemReachedFromTop(lastVisibleItem);
                    }

                    if ( scrollUp ) {
                        onItemReachedFromBottom(lastVisibleItem);
                    }

                    break;
            }
        }

        this.firstVisibleItem.set(firstVisibleItem);

    }

    protected void onItemReachedFromTop(int position) {
    }

    protected void onItemReachedFromBottom(int position) {
    }

    protected void onListViewBottomReached() {
    }

    protected void onListViewTopReached() {
    }

    private class UiThreadUserEventListener implements UserEventListener {

        @Override
        public void onUserEvent(@NotNull final User eventUser, @NotNull final UserEventType userEventType, final @Nullable Object data) {
            new UiThreadRunnable(getActivity(), new Runnable() {
                @Override
                public void run() {
                    AbstractMessengerListFragment.this.adapter.onUserEvent(eventUser, userEventType, data);
                }
            }).run();
        }
    }
}
