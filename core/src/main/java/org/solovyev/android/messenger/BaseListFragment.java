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

package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListViewScroller;
import org.solovyev.android.list.ListViewScrollerListener;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.view.MessengerListItem;
import org.solovyev.android.messenger.view.PublicPullToRefreshListView;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.OnRefreshListener2Adapter;
import org.solovyev.common.listeners.AbstractJEventListener;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.Gravity.CENTER;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.View.GONE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.FrameLayout.LayoutParams;
import static android.widget.LinearLayout.VERTICAL;
import static org.solovyev.android.messenger.AdapterSelection.newSelection;
import static org.solovyev.android.messenger.AdapterSelection.restoreSelection;
import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.common.text.Strings.isEmpty;

public abstract class BaseListFragment<LI extends MessengerListItem>
		extends RoboSherlockListFragment
		implements ListViewScrollerListener, ListViewFilter.FilterableListView {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	protected static final int NOT_SELECTED_POSITION = -1;

	/**
	 * Constants are copied from list fragment, see {@link android.support.v4.app.ListFragment}
	 */
	private static final int INTERNAL_EMPTY_ID = 0x00ff0001;

	private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;

	private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

	private static final String BUNDLE_LISTVIEW_STATE = "listview_state";

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

	/**
	 * Filter for list view, null if filter is disabled for current list fragment
	 */
	@Nullable
	private final ListViewFilter listViewFilter;

	@Nonnull
	private BaseListItemAdapter<LI> adapter;

	@Nonnull
	protected final String tag;

	private final int titleResId;

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
	private final Handler uiHandler = App.getUiHandler();

	@Nonnull
	private Context themeContext;

	@Nonnull
	private RoboListeners listeners;

	/**
	 * Last saved instance state (the last state which has been passed through onCreate method)
	 * <p/>
	 * Problem: When fragment is in back stack and has not been shown during activity lifecycle then
	 * onSaveInstanceState() saves nothing on activity destruction (as no view has been created). If later we return to this fragment using back button we loose state.
	 * <p/>
	 * Solution: Save last state and if fragment hsa not been shown - use it.
	 */
	@Nullable
	private Bundle lastSavedInstanceState;

	private boolean viewWasCreated = false;

	@Nullable
	private AdapterSelection<LI> restoredAdapterSelection;

	@Nullable
	private Parcelable restoredListViewState;

	private boolean onListLoadedCallNeeded = false;

	@Nullable
	private BaseListFragment<LI>.AdapterChangedObserver adapterChangedObserver;


    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public BaseListFragment(@Nonnull String tag, int titleResId, boolean filterEnabled, boolean selectFirstItemByDefault) {
		this.titleResId = titleResId;
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
	protected BaseListItemAdapter<LI> getAdapter() {
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

	@Nullable
	protected ListView getListViewById() {
		final View view = getView();
		if (view != null) {
			return getListView(view);
		} else {
			return null;
		}
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

	public boolean isSelectFirstItemByDefault() {
		return selectFirstItemByDefault;
	}

	@Nonnull
	public Handler getUiHandler() {
		return uiHandler;
	}

	@Nonnull
	public Context getThemeContext() {
		return themeContext;
	}

	@Nullable
	protected CharSequence getFilterText() {
		return listViewFilter != null ? listViewFilter.getFilterText() : null;
	}

	public boolean isViewWasCreated() {
		return viewWasCreated;
	}

	public void setOnListLoadedCallNeeded(boolean onListLoadedCallNeeded) {
		this.onListLoadedCallNeeded = onListLoadedCallNeeded;
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
		themeContext = new ContextThemeWrapper(activity, App.getTheme().getContentThemeResId());
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (listViewFilter != null) {
			listViewFilter.onCreate(savedInstanceState);
		}

		listeners = new RoboListeners(App.getEventManager(getActivity()));

		createAdapter(savedInstanceState);

		lastSavedInstanceState = savedInstanceState;
		viewWasCreated = false;
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
		Log.d(tag, "onCreateView");
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

		if (!getMultiPaneManager().isDualPane(getActivity())) {
			// only one pane is shown => can update action bar options
			updateActionBar();
		} else {
			// several panes are shown
			if (getId() != R.id.content_first_pane) {
				updateActionBar();
			}
		}

		multiPaneManager.onCreatePane(getActivity(), container, root);

		initViewStates(savedInstanceState);

		return root;
	}

	private void updateActionBar() {
		final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(titleResId);
		actionBar.setIcon(R.drawable.mpp_app_icon);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		Log.d(tag, "onViewCreated");
		viewWasCreated = true;

		if (listViewFilter != null) {
			listViewFilter.onViewCreated();
		}

		final ListView listView = getListViewById();
		if (listView != null) {
			listView.setOnItemClickListener(new ListViewOnItemClickListener());
			listView.setOnItemLongClickListener(new ListViewOnItemLongClickListener());
		}

		if (onListLoadedCallNeeded) {
			onListLoaded();
		}
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

	private void createAdapter(@Nullable Bundle savedInstanceState) {
		adapter = createAdapter();
		adapter.registerDataSetObserver(new EmptyAdapterObserver());

		if (savedInstanceState != null) {
			adapter.restoreState(savedInstanceState);
		}

		setListAdapter(adapter);
	}

	private void initViewStates(@Nullable Bundle savedInstanceState) {
		if (restoredAdapterSelection == null) {
			final AdapterSelection<LI> selection = adapter.getSelectionHelper().getSelection();
			final int position = selection.getPosition();

			if (position < 0) {
				final int defaultPosition = isSelectFirstItemByDefault() ? 0 : NOT_SELECTED_POSITION;
				if (savedInstanceState != null) {
					restoredAdapterSelection = restoreSelection(savedInstanceState, defaultPosition);
				} else {
					restoredAdapterSelection = newSelection(defaultPosition, null, null);
				}
			} else {
				// adapter already has selection
				restoredAdapterSelection = selection;
			}
		}

		if (restoredListViewState == null) {
			if (savedInstanceState != null) {
				restoredListViewState = savedInstanceState.getParcelable(BUNDLE_LISTVIEW_STATE);
			} else {
				restoredListViewState = null;
			}
		}

		Log.d(tag, "Restoring view states:\nselection=" + restoredAdapterSelection + "\nlist view state=" + restoredListViewState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (viewWasCreated) {
			adapter.saveState(outState);

			if (listViewFilter != null) {
				listViewFilter.saveState(outState);
			}

			final Parcelable listViewState = createListViewState();
			if (listViewState != null) {
				outState.putParcelable(BUNDLE_LISTVIEW_STATE, listViewState);
			} else {
				outState.putParcelable(BUNDLE_LISTVIEW_STATE, restoredListViewState);
			}
		} else {
			if (lastSavedInstanceState != null) {
				outState.putAll(lastSavedInstanceState);
			}
		}

		lastSavedInstanceState = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		onListLoadedCallNeeded = true;
	}

	@Nullable
	private Parcelable createListViewState() {
		final ListView listView = getListViewById();
		if (listView != null) {
			return listView.onSaveInstanceState();
		}

		return null;
	}

	@Override
	public void onPause() {
		listeners.clearAll();
		restoredAdapterSelection = adapter.getSelectionHelper().getSelection();
		restoredListViewState = createListViewState();
		Log.d(tag, "Saving view states:\nselection=" + restoredAdapterSelection + "\nlist view state=" + restoredListViewState);
		super.onPause();
	}

	public void filter(@Nullable CharSequence filterText) {
		adapter.filter(filterText == null ? null : filterText.toString(), null);
	}

	protected void onListLoaded() {
		Log.d(tag, "onListLoaded");
		final Activity activity = getActivity();

		if (activity != null && !activity.isFinishing() && !isDetached() && viewWasCreated) {
			Log.d(tag, "Restoring list properties");
			restoreListViewState();

			if (restoredAdapterSelection != null) {
				restoreAdapterSelection(activity, restoredAdapterSelection);
			}
			onListLoadedCallNeeded = false;
		} else {
			Log.d(tag, "onListLoaded should be called again");
			onListLoadedCallNeeded = true;
		}
	}

	protected void restoreAdapterSelection(@Nonnull Activity activity, @Nonnull AdapterSelection<LI> selection) {
		if (adapter.isSaveSelection()) {
			final String selectedItemId = selection.getId();
			final int selectedPosition = selection.getPosition();

			int position = -1;
			if (selectedItemId != null) {
				position = adapter.getPositionById(selectedItemId);
			}

			if (position < 0) {
				position = selectedPosition;
			}

			Log.d(tag, "Restoring adapter: position=" + position);
			initialClickItem(activity, position, adapter);

			registerAdapterChangedObserver();
		} else {
			Log.d(tag, "Adapter doesn't save selection");
		}
	}

	protected void restoreListViewState() {
		final ListView listView = getListViewById();
		final Parcelable listViewState = restoredListViewState;
		if (listView != null) {
			Log.d(tag, "Restoring list view(size=" + listView.getCount() + "): " + listViewState);
			if (listViewState != null) {
				listView.onRestoreInstanceState(listViewState);
			} else {
				if (isScrollToEndByDefault()) {
					final int count = getAdapter().getCount();
					if (count > 0) {
						listView.setSelection(count - 1);
					}
				}
			}
		}
	}

	protected boolean isScrollToEndByDefault() {
		return false;
	}

	@Nonnull
	protected abstract BaseListItemAdapter<LI> createAdapter();

	protected void onEvent(@Nonnull AccountEvent event) {
	}

	protected final void unregisterAdapterChangedObserver() {
		if (adapterChangedObserver != null) {
			adapter.unregisterDataSetObserver(adapterChangedObserver);
			adapterChangedObserver = null;
		}
	}

	protected final void registerAdapterChangedObserver() {
		if (adapterChangedObserver == null) {
			adapterChangedObserver = new AdapterChangedObserver();
			adapter.registerDataSetObserver(adapterChangedObserver);
		}
	}

    /*
    **********************************************************************
    *
    *                           SCROLLING
    *
    **********************************************************************
    */


	public void onItemReachedFromTop(int position, int total) {
	}

	public void onItemReachedFromBottom(int position, int total) {
	}

	public void onBottomReached() {
	}

	public void onTopReached() {
	}

	public final boolean clickItemById(@Nonnull String listItemId) {
		final int size = adapter.getCount();

		for (int i = 0; i < size; i++) {
			final MessengerListItem listItem = adapter.getItem(i);
			if (listItem.getId().equals(listItemId)) {
				clickItem(i);
				return true;
			}
		}

		return false;
	}

	private void clickItem(int position) {
		clickItem(this.getActivity(), position, adapter);
	}

	protected void initialClickItem(@Nonnull final Activity activity, final int position, @Nonnull final BaseListItemAdapter<LI> adapter) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {

				Log.d(tag, "Initial click: position=" + position);
				if (position >= 0 && position < adapter.getCount()) {
					adapter.getSelectionHelper().onItemClick(position);
				}

				if (getMultiPaneManager().isDualPane(activity)) {
					if (adapter.isEmpty()) {
						onEmptyList((BaseFragmentActivity) activity);
					} else if (!canReuseFragment((FragmentActivity) activity, adapter.getSelectedItem())) {
						// in case of dual pane we need to make a real click (call click listener)
						clickItem(activity, position, adapter);
					}
				}

				Log.d(tag, "Selection after initial click: " + adapter.getSelectionHelper().getSelection());
			}
		});
	}

	private void clickItem(@Nonnull Activity activity, int position, @Nonnull BaseListItemAdapter<LI> adapter) {
		if (position >= 0 && position < adapter.getCount()) {
			adapter.getSelectionHelper().onItemClick(position);

			if (getMultiPaneManager().isDualPane(activity)) {
				final ListItem.OnClickAction onClickAction = adapter.getItem(position).getOnClickAction();
				if (onClickAction != null) {
					onClickAction.onClick(activity, adapter);
				}
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

	protected class UserEventListener extends AbstractJEventListener<UserEvent> {

		protected UserEventListener() {
			super(UserEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final UserEvent event) {
			adapter.onEvent(event);
		}
	}

	protected class AccountEventListener extends AbstractJEventListener<AccountEvent> {

		protected AccountEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final AccountEvent event) {
			BaseListFragment.this.onEvent(event);
			adapter.onEvent(event);
		}
	}

	protected void onEmptyList(@Nonnull BaseFragmentActivity activity) {
		if (getId() == R.id.content_first_pane) {
			if (getMultiPaneManager().isDualPane(activity)) {
				final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
				mpfm.emptifySecondFragment();
				if (multiPaneManager.isTriplePane(activity)) {
					mpfm.emptifyThirdFragment();
				}
			}

			updateActionBar();
		}
	}

	private boolean canReuseFragment(@Nonnull FragmentActivity activity, @Nullable ListItem selectedItem) {
		final Fragment fragmentById = activity.getSupportFragmentManager().findFragmentById(R.id.content_second_pane);
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

				adapter.getSelectionHelper().onItemClick(listItem);

				final ListItem.OnClickAction onClickAction = listItem.getOnClickAction();
				if (onClickAction != null) {
					onClickAction.onClick(getActivity(), adapter);
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

				final ListItem.OnClickAction onClickAction = listItem.getOnLongClickAction();
				if (onClickAction != null) {
					onClickAction.onClick(getActivity(), adapter);
					return true;
				}
			}

			return false;
		}
	}

	private class AdapterChangedObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			if (!adapter.isEmpty()) {
				final ListItemAdapterSelectionHelper<LI> sh = adapter.getSelectionHelper();

				// if adapter has changed we need to check that our saved selected position is still the same
				final AdapterSelection<LI> selection = sh.getSelection();
				if (!selection.isAlreadySelected(adapter)) {
					// position has changed => we need to reselect list item
					final LI listItem = selection.getItem();
					if (!sh.findAndSelectItem(listItem)) {
						// item can't be reselected => item is removed
						// in that case let's select item on the same position or the first element if such selection can't be done
						final int position = selection.getPosition();
						if (position >= 0 && position < adapter.getCount()) {
							clickItem(position);
						} else if (!adapter.isEmpty()) {
							clickItem(0);
						}
					}
				}
			}
		}
	}

	private class EmptyAdapterObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			if (adapter.isEmpty()) {
				final BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
				if (activity != null) {
					if (isEmpty(adapter.getFilterText())) {
						// only for lists which have become empty because of lack of items (and not because of filter)
						onEmptyList(activity);
					}
				}
			}
		}
	}
}
