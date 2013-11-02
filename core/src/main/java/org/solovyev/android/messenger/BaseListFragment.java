package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.solovyev.android.Threads;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListViewScroller;
import org.solovyev.android.list.ListViewScrollerListener;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.FragmentUiEventType;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.android.messenger.view.PublicPullToRefreshListView;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.OnRefreshListener2Adapter;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.View.GONE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.FrameLayout.LayoutParams;
import static android.widget.LinearLayout.VERTICAL;
import static org.solovyev.android.messenger.App.newTag;

public abstract class BaseListFragment<T, LI extends MessengerListItem>
		extends RoboSherlockListFragment
		implements ListViewScrollerListener, ListViewFilter.FilterableListView {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final int NOT_SELECTED_POSITION = -1;

	/**
	 * Constants are copied from list fragment, see {@link android.support.v4.app.ListFragment}
	 */
	private static final int INTERNAL_EMPTY_ID = 0x00ff0001;

	private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;

	private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */


	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private MessageService messageService;

	@Inject
	@Nonnull
	private SyncService syncService;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private EventManager eventManager;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

	@Nullable
	private JEventListener<UserEvent> userEventListener;

	private BaseListItemAdapter<LI> adapter;

	@Nullable
	private MessengerAsyncTask<Void, Void, List<T>> listLoader;

	/**
	 * Filter for list view, null if filter is disabled for current list fragment
	 */
	@Nullable
	private final ListViewFilter listViewFilter;

	@Nonnull
	private final String tag;

	@Nullable
	private PublicPullToRefreshListView pullToRefreshListView;

	/**
	 * Mode which is used for {@link PullToRefreshListView}.
	 * Note: null if simple {@link ListView} is used instead of {@link PullToRefreshListView}.
	 */
	@SuppressWarnings("FieldCanBeLocal")
	@Nullable
	private PullToRefreshBase.Mode pullToRefreshMode;


	/**
	 * If nothing selected - first list item will be selected
	 */
	private final boolean selectFirstItemByDefault;

	@Nonnull
	private final Handler uiHandler = Threads.newUiHandler();

	@Nonnull
	private PostListLoadingRunnable onPostLoading;

	@Nonnull
	private Context themeContext;

	@Nonnull
	private RoboListeners listeners;

	/**
	 * Last saved instance state (the last state which has been passed through onCreate method)
	 *
	 * Problem: When fragment is in back stack and has not been shown during activity lifecycle then
	 * onSaveInstanceState() saves nothing on activity destruction (as no view has been created). If later we return to this fragment using back button we loose state.
	 *
	 * Solution: Save last state and if fragment hsa not been shown - use it.
	 */
	@Nullable
	private Bundle lastSavedInstanceState;

	private boolean viewCreated = false;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public BaseListFragment(@Nonnull String tag, boolean filterEnabled, boolean selectFirstItemByDefault) {
		this.tag = newTag(tag);
		if (filterEnabled) {
			this.listViewFilter = new ListViewFilter(this, this);
		} else {
			this.listViewFilter = null;
		}
		this.selectFirstItemByDefault = selectFirstItemByDefault;
	}

    /*
    **********************************************************************
    *
    *                           GETTERS
    *
    **********************************************************************
    */

	@Nonnull
	protected UserService getUserService() {
		return userService;
	}

	@Nonnull
	protected SyncService getSyncService() {
		return syncService;
	}

	@Nonnull
	protected ChatService getChatService() {
		return chatService;
	}

	@Nonnull
	protected MessageService getMessageService() {
		return messageService;
	}

	@Nonnull
	protected AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	protected BaseListItemAdapter getAdapter() {
		return adapter;
	}

	@Nonnull
	protected EventManager getEventManager() {
		return eventManager;
	}

	@Nonnull
	protected MultiPaneManager getMultiPaneManager() {
		return multiPaneManager;
	}

	@Nonnull
	protected ListView getListView(@Nonnull View root) {
		return (ListView) root.findViewById(android.R.id.list);
	}

	@Nullable
	public PublicPullToRefreshListView getPullToRefreshListView() {
		return pullToRefreshListView;
	}

	@Nonnull
	protected RoboListeners getListeners() {
		return listeners;
	}

	/*
    **********************************************************************
    *
    *                           LIFECYCLE
    *
    **********************************************************************
    */

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		themeContext = new ContextThemeWrapper(activity, R.style.mpp_theme_metro_fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listeners = new RoboListeners(App.getEventManager(getActivity()));
		eventManager.fire(FragmentUiEventType.created.newEvent(this));

		lastSavedInstanceState = savedInstanceState;
		viewCreated = false;
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewCreated = true;
		final LinearLayout root = new LinearLayout(themeContext);
		root.setOrientation(VERTICAL);

		if (listViewFilter != null) {
			final View filterView = listViewFilter.createView(savedInstanceState);
			root.addView(filterView, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
		}

		final View listViewParent = createListView();

		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f);
		params.gravity = CENTER_VERTICAL;
		root.addView(listViewParent, params);

		// some fragments may change the title and icon of action bar => we need to reset it every time new fragment is shown
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(R.string.mpp_app_name);
		actionBar.setIcon(R.drawable.mpp_app_icon);

		multiPaneManager.onCreatePane(getActivity(), container, root);

		return root;
	}

	@Nonnull
	public Context getThemeContext() {
		return themeContext;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		if (listViewFilter != null) {
			listViewFilter.onViewCreated();
		}

		eventManager.fire(FragmentUiEventType.shown.newEvent(this));
	}

	public void toggleFilterBox() {
		if (listViewFilter != null) {
			listViewFilter.toggleView();
		}
	}

	protected void setFilterBoxVisible() {
		if (listViewFilter != null) {
			listViewFilter.setFilterBoxVisible(true);
		}
	}

	@Nonnull
	private View createListView() {
		final Context context = getThemeContext();

		final FrameLayout root = new FrameLayout(context);

		// ------------------------------------------------------------------

		final LinearLayout progressContainer = new LinearLayout(context);
		progressContainer.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		progressContainer.setOrientation(VERTICAL);
		progressContainer.setVisibility(GONE);
		progressContainer.setGravity(CENTER);

		final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		progressContainer.addView(progress, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

		root.addView(progressContainer, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

		// ------------------------------------------------------------------

		final FrameLayout listViewContainer = new FrameLayout(context);
		listViewContainer.setId(INTERNAL_LIST_CONTAINER_ID);

		final TextView emptyListCaption = new TextView(context);
		emptyListCaption.setId(INTERNAL_EMPTY_ID);
		emptyListCaption.setGravity(CENTER);
		listViewContainer.addView(emptyListCaption, new LayoutParams(MATCH_PARENT, MATCH_PARENT));


		final ListViewAwareOnRefreshListener topRefreshListener = getTopPullRefreshListener();
		final ListViewAwareOnRefreshListener bottomRefreshListener = getBottomPullRefreshListener();

		final View listView;

		final Resources resources = context.getResources();
		if (topRefreshListener == null && bottomRefreshListener == null) {
			pullToRefreshMode = null;
			listView = new ListView(context);
			fillListView((ListView) listView, context);
			listView.setId(android.R.id.list);
		} else if (topRefreshListener != null && bottomRefreshListener != null) {
			pullToRefreshMode = PullToRefreshBase.Mode.BOTH;
			pullToRefreshListView = new PublicPullToRefreshListView(context, pullToRefreshMode);

			topRefreshListener.setListView(pullToRefreshListView);
			bottomRefreshListener.setListView(pullToRefreshListView);

			fillListView(pullToRefreshListView.getRefreshableView(), context);
			pullToRefreshListView.setShowIndicator(false);

			pullToRefreshListView.setOnRefreshListener(new OnRefreshListener2Adapter(topRefreshListener, bottomRefreshListener));
			listView = pullToRefreshListView;
		} else if (topRefreshListener != null) {
			pullToRefreshMode = PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH;
			pullToRefreshListView = new PublicPullToRefreshListView(context, pullToRefreshMode);

			topRefreshListener.setListView(pullToRefreshListView);

			fillListView(pullToRefreshListView.getRefreshableView(), context);

			pullToRefreshListView.setShowIndicator(false);

			pullToRefreshListView.setOnRefreshListener(topRefreshListener);

			listView = pullToRefreshListView;
		} else {
			pullToRefreshMode = PullToRefreshBase.Mode.PULL_UP_TO_REFRESH;
			pullToRefreshListView = new PublicPullToRefreshListView(context, pullToRefreshMode);

			bottomRefreshListener.setListView(pullToRefreshListView);

			fillListView(pullToRefreshListView.getRefreshableView(), context);
			pullToRefreshListView.setShowIndicator(false);

			pullToRefreshListView.setOnRefreshListener(bottomRefreshListener);

			listView = pullToRefreshListView;
		}

		listViewContainer.addView(listView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

		root.addView(listViewContainer, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

		return root;
	}

	protected void fillListView(@Nonnull ListView lv, @Nonnull Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			lv.setScrollbarFadingEnabled(true);
		}
		lv.setBackgroundDrawable(null);
		lv.setCacheColorHint(Color.TRANSPARENT);
		ListViewScroller.createAndAttach(lv, this);
		lv.setFastScrollEnabled(true);

		lv.setTextFilterEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			lv.setOverscrollFooter(null);
		}

		lv.setVerticalFadingEdgeEnabled(false);
		lv.setFocusable(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
		}
		lv.setDivider(new ColorDrawable(Color.LTGRAY));
		lv.setDividerHeight(1);
	}

	@Nullable
	protected abstract ListViewAwareOnRefreshListener getTopPullRefreshListener();

	@Nullable
	protected abstract ListViewAwareOnRefreshListener getBottomPullRefreshListener();

	protected void addFooterButton(@Nonnull ViewGroup root, int textResId, @Nonnull View.OnClickListener onClick) {
		final Button footerButton = ViewFromLayoutBuilder.<Button>newInstance(R.layout.mpp_fragment_footer_button).build(getThemeContext());

		footerButton.setText(textResId);
		footerButton.setOnClickListener(onClick);

		root.addView(footerButton, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		userEventListener = new UiThreadUserEventListener();
		userService.addListener(userEventListener);

		final ListView lv = getListView();
		lv.setOnItemClickListener(new ListViewOnItemClickListener());
		lv.setOnItemLongClickListener(new ListViewOnItemLongClickListener());

		// as newly loaded list can differ from one used last time position may be not accurate
		// better approach is to use list item which was previously selected and reuse it
		Integer selectedPosition = null;
		ListItem selectedListItem = null;
		if (adapter != null) {
			// adapter not null => this fragment has been created earlier (and now it just goes to the shown state)
			selectedPosition = adapter.getSelectedItemPosition();
			if (selectedPosition == NOT_SELECTED_POSITION && selectFirstItemByDefault) {
				// there were no elements in adapter => position == NOT_SELECTED_POSITION
				// but we need to select first element if selectFirstItemByDefault == true => do it
				selectedPosition = 0;
			} else if (selectedPosition >= 0 && selectedPosition < adapter.getCount()) {
				// selected position exists => exists selected list item => can use it
				selectedListItem = adapter.getItem(selectedPosition);
			}
		}

		adapter = createAdapter();
		if (savedInstanceState != null) {
			adapter.restoreState(savedInstanceState);
		}

		setListAdapter(adapter);

		if (selectedPosition == null) {
			if (savedInstanceState != null) {
				selectedPosition = adapter.restoreSelectedPosition(savedInstanceState, selectFirstItemByDefault ? 0 : NOT_SELECTED_POSITION);
			} else {
				selectedPosition = selectFirstItemByDefault ? 0 : NOT_SELECTED_POSITION;
			}
		}

		onPostLoading = new PostListLoadingRunnable(selectedPosition, selectedListItem, getListView());
	}

	@Override
	public void onStart() {
		super.onStart();

		this.listLoader = createAsyncLoader(adapter, onPostLoading);
		if (this.listLoader != null) {
			this.listLoader.executeInParallel();
		} else {
			// we need to schedule onPostLoading in order to be after all pending transaction in fragment manager
			uiHandler.post(onPostLoading);
		}

		eventManager.fire(FragmentUiEventType.started.newEvent(this));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (viewCreated) {
			if (adapter != null) {
				adapter.saveState(outState);
			}

			if (listViewFilter != null) {
				listViewFilter.saveState(outState);
			}
		} else {
			if(lastSavedInstanceState != null) {
				outState.putAll(lastSavedInstanceState);
			}
		}

		lastSavedInstanceState = null;
	}

	@Override
	public void onPause() {
		listeners.clearAll();
		super.onPause();
	}

	@Override
	public void onStop() {
		if (listLoader != null) {
			listLoader.cancel(false);
			listLoader = null;
		}

		super.onStop();
	}

	@Nullable
	protected CharSequence getFilterText() {
		return listViewFilter != null ? listViewFilter.getFilterText() : null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (userEventListener != null) {
			this.userService.removeListener(userEventListener);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void filter(@Nullable CharSequence filterText) {
		filter(filterText, null);
	}

	public void filter(@Nullable  CharSequence filterText, @Nullable Filter.FilterListener filterListener) {
		if (this.adapter != null && this.adapter.isInitialized()) {
			this.adapter.filter(filterText == null ? null : filterText.toString(), filterListener);
		}
	}

	@Nonnull
	protected abstract BaseListItemAdapter<LI> createAdapter();

	@Nullable
	protected abstract MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter, @Nonnull Runnable onPostExecute);

	@Nullable
	protected MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull BaseListItemAdapter<LI> adapter) {
		return createAsyncLoader(adapter, new EmptyRunnable());
	}

    /*
    **********************************************************************
    *
    *                           SCROLLING
    *
    **********************************************************************
    */


	public void onItemReachedFromTop(int position) {
	}

	public void onItemReachedFromBottom(int position) {
	}

	public void onBottomReached() {
	}

	public void onTopReached() {
	}

	public final void clickItemById(@Nonnull String listItemId) {
		if (adapter != null && adapter.isInitialized()) {
			final int size = adapter.getCount();
			for (int i = 0; i < size; i++) {
				final MessengerListItem listItem = adapter.getItem(i);
				if (listItem.getId().equals(listItemId)) {
					clickItem(i);
					break;
				}
			}
		}
	}

	private void clickItem(int position) {
		final View root = getView();
		if (root != null) {
			clickItem(this.getActivity(), position, getListView(root));
		}
	}

	private void clickItem(@Nonnull Activity activity, int position, @Nonnull ListView listView) {
		if (position >= 0 && position < adapter.getCount()) {
			adapter.getSelectedItemListener().onItemClick(position);
			final ListItem.OnClickAction onClickAction = adapter.getItem(position).getOnClickAction();
			if (onClickAction != null) {
				onClickAction.onClick(activity, adapter, listView);
			}
		}
	}

    /*
    **********************************************************************
    *
    *                           LISTENERS, HELPERS, ETC
    *
    **********************************************************************
    */

	private class UiThreadUserEventListener extends AbstractJEventListener<UserEvent> {

		private UiThreadUserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final UserEvent event) {
			Threads2.tryRunOnUiThread(BaseListFragment.this, new Runnable() {
				@Override
				public void run() {
					adapter.onEvent(event);
				}
			});
		}
	}

	private class PostListLoadingRunnable implements Runnable {

		private final int selectedPosition;

		@Nullable
		private final ListItem selectedListItem;

		@Nonnull
		private final ListView listView;

		public PostListLoadingRunnable(int selectedPosition, @Nullable ListItem selectedListItem, @Nonnull ListView lv) {
			this.selectedPosition = selectedPosition;
			this.selectedListItem = selectedListItem;
			this.listView = lv;
		}

		@Override
		public void run() {
			// change adapter state
			adapter.setInitialized(true);

			final Activity activity = getActivity();
			if (activity != null) {

				try {
					runPostFilling();
				} catch (IllegalStateException e) {
					// todo serso: investigate the root of the problem
					// oops, list view is not created yet
					Log.w(tag, e.getMessage(), e);
				}
			}
		}

		private void runPostFilling() {
			// apply filter if any
			if (listViewFilter != null) {
				filter(listViewFilter.getFilterText(), new PostListLoadingFilterListener());
			} else {
				filter(null, new PostListLoadingFilterListener());
			}
		}

		private final class PostListLoadingFilterListener implements Filter.FilterListener {

			@Override
			public void onFilterComplete(int count) {
				final Activity activity = getActivity();
				if (activity != null && !activity.isFinishing() && !isDetached()) {

					int position = -1;
					if (selectedListItem != null) {
						position = adapter.getSelectedItemListener().onItemClick(selectedListItem);
					} else {
						if (selectedPosition >= 0 && selectedPosition < adapter.getCount()) {
							adapter.getSelectedItemListener().onItemClick(selectedPosition);
						}
					}

					if (position < 0) {
						position = selectedPosition;
					}

					if(multiPaneManager.isDualPane(activity)) {
						if (adapter.isEmpty()) {
							onEmptyListLoaded((BaseFragmentActivity) activity);
						} else if (!canReuseFragment(adapter.getSelectedItem())) {
							// in case of dual pane we need to make a real click (call click listener)
							clickItem(activity, position, listView);
						}
					}
				}
			}
		}
	}

	protected void onEmptyListLoaded(@Nonnull BaseFragmentActivity activity) {
		activity.getMultiPaneFragmentManager().emptifySecondFragment();
		if(multiPaneManager.isTriplePane(activity)) {
			activity.getMultiPaneFragmentManager().emptifyThirdFragment();
		}
	}

	private boolean canReuseFragment(@Nullable ListItem selectedItem) {
		final Fragment fragmentById = getActivity().getSupportFragmentManager().findFragmentById(R.id.content_second_pane);
		if (fragmentById == null || selectedItem == null) {
			return false;
		} else {
			return canReuseFragment(fragmentById, (LI) selectedItem);
		}
	}

	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull LI selectedItem) {
		return false;
	}

	private class ListViewOnItemClickListener implements AdapterView.OnItemClickListener {

		public void onItemClick(final AdapterView<?> parent,
								final View view,
								final int position,
								final long id) {
			final Object itemAtPosition = parent.getItemAtPosition(position);

			if (itemAtPosition instanceof ListItem) {
				final ListItem listItem = (ListItem) itemAtPosition;
				// notify adapter

				adapter.getSelectedItemListener().onItemClick(listItem);

				final ListItem.OnClickAction onClickAction = listItem.getOnClickAction();
				if (onClickAction != null) {
					onClickAction.onClick(getActivity(), adapter, getListView());
				}
			}
		}
	}

	private class ListViewOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			final Object itemAtPosition = parent.getItemAtPosition(position);

			if (itemAtPosition instanceof ListItem) {
				final ListItem listItem = (ListItem) itemAtPosition;
				// notify adapter

				final ListItem.OnClickAction onClickAction = listItem.getOnLongClickAction();
				if (onClickAction != null) {
					onClickAction.onClick(getActivity(), adapter, getListView());
					return true;
				}
			}

			return false;
		}
	}

	private static class EmptyRunnable implements Runnable {
		@Override
		public void run() {
		}
	}
}
