package org.solovyev.android.messenger;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.*;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.OnRefreshListener2Adapter;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:57 PM
 */
public abstract class AbstractMessengerListFragment<T> extends RoboSherlockListFragment implements AbsListView.OnScrollListener {

    @NotNull
    private static final String FILTER = "filter";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private AuthServiceFacade authServiceFacade;

    @Inject
    @NotNull
    private SyncService syncService;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

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

    private boolean dualPane;

    public AbstractMessengerListFragment(@NotNull String tag) {
        this.tag = tag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate: " + this);

        try {
            this.user = this.authServiceFacade.getUser(getActivity());
        } catch (UserIsNotLoggedInException e) {
            MessengerLoginActivity.startActivity(getActivity());
        }
    }

    @NotNull
    protected UserService getUserService() {
        return userService;
    }

    @NotNull
    protected AuthServiceFacade getAuthServiceFacade() {
        return authServiceFacade;
    }

    @NotNull
    protected SyncService getSyncService() {
        return syncService;
    }

    @NotNull
    protected ChatService getChatService() {
        return chatService;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "onCreateView: " + this);

        final LinearLayout root = new LinearLayout(this.getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        if (isFilterEnabled()) {
            final ViewGroup filterBoxParent = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.msg_list_filter).build(this.getActivity());

            filterInput = (EditText) filterBoxParent.findViewById(R.id.filter_box);
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

            root.addView(filterBoxParent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        final View listViewParent = createListView(inflater, container);
        final ListView listView = getListView(listViewParent);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        root.addView(listViewParent, params);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isFilterEnabled()) {
            if ( savedInstanceState != null ) {
                final String filter = savedInstanceState.getString(FILTER);
                if (StringUtils.isEmpty(filter)) {
                    setFilterBoxVisible(false);
                } else{
                    filterInput.setText(filter);
                    setFilterBoxVisible(true);
                }
            } else {
                setFilterBoxVisible(false);
            }
        }
    }

    @NotNull
    protected ListView getListView(@NotNull View root) {
        return (ListView) root.findViewById(android.R.id.list);
    }

    protected abstract boolean isFilterEnabled();

    public void toggleFilterBox() {
        if (isFilterEnabled()) {
            assert filterInput != null;

            final ViewGroup filterBox = (ViewGroup) getView().findViewById(R.id.filter_box_parent);
            if (filterBox != null) {
                int visibility = filterBox.getVisibility();

                if (visibility != View.VISIBLE) {
                    filterBox.setVisibility(View.VISIBLE);
                    filterInput.requestFocus();

                    final InputMethodManager manager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.showSoftInput(filterInput, InputMethodManager.SHOW_IMPLICIT);

                } else if (visibility != View.GONE) {
                    // if filter box is visible before hiding it clear filter query
                    filterInput.getText().clear();
                    filterInput.clearFocus();

                    filterBox.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setFilterBoxVisible(boolean visible) {
        if (isFilterEnabled()) {
            final ViewGroup filterBox = (ViewGroup) getView().findViewById(R.id.filter_box_parent);
            if (filterBox != null) {
                setFilterBoxVisible(visible, filterBox);
            }
        }
    }

    private void setFilterBoxVisible(boolean visible, @NotNull ViewGroup filterBox) {
        if (visible) {
            filterBox.setVisibility(View.VISIBLE);
        } else {
            filterBox.setVisibility(View.GONE);
        }
    }

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
        lv.setDividerHeight(1);
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
        this.userService.addUserEventListener(userEventListener);

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

        final View rightContentPane = getActivity().findViewById(R.id.right_content_pane);
        dualPane = rightContentPane != null && rightContentPane.getVisibility() == View.VISIBLE;
        if (isDualPane()) {
            ContactDualPaneController.getInstance().registerDualPaneFragment(this);
        }

        if (savedInstanceState != null) {
            final int position = savedInstanceState.getInt("position", 0);
            setSelection(position);
        }

        if (isDualPane()) {
            // In dual-pane mode, list view highlights selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }


    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (isDualPane()) {
            updateRightPane();
        }
    }

    protected abstract void updateRightPane();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("position", getSelectedItemPosition());

        if (isFilterEnabled()) {
            outState.putString(FILTER, filterInput.getText().toString());
        }
    }

    public boolean isDualPane() {
        return dualPane;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listLoader != null) {
            listLoader.cancel(false);
        }

        if (userEventListener != null) {
            this.userService.removeUserEventListener(userEventListener);
        }
    }

    @Override
    public void onDestroy() {
        if (isDualPane()) {
            ContactDualPaneController.getInstance().unregisterDualPaneFragment(this);
        }

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
                        onListViewTopReached();
                    } else {
                        if (scrollDown && lastVisibleItem == totalItemCount) {
                            // reach bottom
                            onListViewBottomReached();
                        }
                    }

                    if (scrollDown) {
                        onItemReachedFromTop(lastVisibleItem);
                    }

                    if (scrollUp) {
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
