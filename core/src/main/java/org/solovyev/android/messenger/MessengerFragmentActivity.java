package org.solovyev.android.messenger;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.MessengerPrimaryFragment;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.view.SwipeGestureListener;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import static org.solovyev.android.messenger.UiThreadEventListener.onUiThread;
import static org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager.tabFragments;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerFragmentActivity extends RoboSherlockFragmentActivity {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final String SELECTED_NAV = "selected_nav";

	protected final String TAG = this.getClass().getSimpleName();

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private MessengerMultiPaneManager multiPaneManager;

	@Inject
	@Nonnull
	private MessengerListeners messengerListeners;

	@Inject
	@Nonnull
	private UnreadMessagesCounter unreadMessagesCounter;

	@Inject
	@Nonnull
	private NotificationService notificationService;

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

	private int layoutId;

	private boolean showActionBarTabs = true;

	private boolean actionBarIconAsUp = true;

	@Nullable
	private ViewGroup secondPane;

	@Nullable
	private ViewGroup thirdPane;

	@Nonnull
	private final MessengerMultiPaneFragmentManager multiPaneFragmentManager;

	@Nonnull
	private ActivityMenu<Menu, MenuItem> menu;

	@Nullable
	private JEventListener<MessengerEvent> messengerEventListener;

	@Nullable
	private GestureDetector gestureDetector;


    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	protected MessengerFragmentActivity(int layoutId) {
		this.layoutId = layoutId;
		this.multiPaneFragmentManager = new MessengerMultiPaneFragmentManager(this);
	}

	protected MessengerFragmentActivity(int layoutId, boolean showActionBarTabs, boolean actionBarIconAsUp) {
		this.layoutId = layoutId;
		this.showActionBarTabs = showActionBarTabs;
		this.actionBarIconAsUp = actionBarIconAsUp;
		this.multiPaneFragmentManager = new MessengerMultiPaneFragmentManager(this);
	}

    /*
    **********************************************************************
    *
    *                           GETTERS/SETTERS
    *
    **********************************************************************
    */

	@Nonnull
	protected UserService getUserService() {
		return userService;
	}

	@Nonnull
	public ChatService getChatService() {
		return chatService;
	}

	@Nonnull
	public EventManager getEventManager() {
		return eventManager;
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public MessengerListeners getMessengerListeners() {
		return messengerListeners;
	}

	@Nonnull
	public UnreadMessagesCounter getUnreadMessagesCounter() {
		return unreadMessagesCounter;
	}

	@Nonnull
	public MessengerMultiPaneManager getMultiPaneManager() {
		return multiPaneManager;
	}

	public boolean isDualPane() {
		return this.secondPane != null;
	}

	public boolean isTriplePane() {
		return this.thirdPane != null;
	}

	@Nonnull
	public MessengerMultiPaneFragmentManager getMultiPaneFragmentManager() {
		return multiPaneFragmentManager;
	}

    /*
    **********************************************************************
    *
    *                           LIFECYCLE
    *
    **********************************************************************
    */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager.enableDebugLogging(true);

		setContentView(layoutId);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(actionBarIconAsUp);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		this.secondPane = (ViewGroup) findViewById(R.id.content_second_pane);
		this.thirdPane = (ViewGroup) findViewById(R.id.content_third_pane);

		if (showActionBarTabs) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			for (MessengerPrimaryFragment tabFragment : tabFragments) {
				addTab(tabFragment);
			}

			int navPosition = -1;
			if (savedInstanceState != null) {
				navPosition = savedInstanceState.getInt(SELECTED_NAV, -1);
			}

			if (navPosition >= 0) {
				getSupportActionBar().setSelectedNavigationItem(navPosition);
			}

			gestureDetector = new GestureDetector(this, new SwipeGestureListener(this) {

				@Override
				protected void onSwipeToRight() {
					changeTab(false);
				}

				@Override
				protected void onSwipeToLeft() {
					changeTab(true);
				}
			});
		}

		this.menu = new MessengerMenu(new Runnable() {
			@Override
			public void run() {
				if (actionBarIconAsUp) {
					if (!multiPaneFragmentManager.goBackImmediately()) {
						final ActionBar.Tab tab = findTabByTag(MessengerPrimaryFragment.contacts.getFragmentTag());
						if (tab != null) {
							tab.select();
						}
					}
				}
			}
		});

		this.messengerEventListener = onUiThread(this, new MessengerEventListener());
		this.messengerListeners.addListener(messengerEventListener);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final boolean handled = gestureDetector != null && gestureDetector.onTouchEvent(event);
		return handled || super.dispatchTouchEvent(event);
	}

	private void changeTab(boolean next) {
		final ActionBar actionBar = getSupportActionBar();
		final int tabCount = actionBar.getTabCount();

		int position = actionBar.getSelectedNavigationIndex();
		if (next) {
			if (position < tabCount - 1) {
				position = position + 1;
			} else {
				position = 0;
			}
		} else {
			if (position > 0) {
				position = position - 1;
			} else {
				position = tabCount - 1;
			}
		}

		if (position >= 0 && position < tabCount) {
			actionBar.setSelectedNavigationItem(position);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(SELECTED_NAV, getSupportActionBar().getSelectedNavigationIndex());
	}


	private void addTab(@Nonnull final MessengerPrimaryFragment messengerPrimaryFragment) {
		final String fragmentTag = messengerPrimaryFragment.getFragmentTag();

		final ActionBar actionBar = getSupportActionBar();
		final ActionBar.Tab tab = actionBar.newTab();
		tab.setTag(fragmentTag);
		tab.setText(messengerPrimaryFragment.getTitleResId());
		tab.setTabListener(new ActionBar.TabListener() {
			@Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				emptifyNotPrimaryPanes();
				getMultiPaneFragmentManager().setMainFragment(messengerPrimaryFragment, getSupportFragmentManager(), ft);
			}

			@Override
			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
				emptifyNotPrimaryPanes();
				// in some cases we reuse pane for another fragment under same tab -> we need to reset fragment (in case if fragment has not been changed nothing is done)
				getMultiPaneFragmentManager().setMainFragment(messengerPrimaryFragment, getSupportFragmentManager(), ft);
			}
		});
		actionBar.addTab(tab);
	}

	private void emptifyNotPrimaryPanes() {
		if (isDualPane()) {
			getMultiPaneFragmentManager().emptifySecondFragment();
			if (isTriplePane()) {
				getMultiPaneFragmentManager().emptifyThirdFragment();
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (this.messengerEventListener != null) {
			this.messengerListeners.removeListener(messengerEventListener);
		}

		super.onDestroy();
	}

    /*
    **********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return this.menu.onPrepareOptionsMenu(this, menu);
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		return this.menu.onCreateOptionsMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this, item);
	}

	@Nullable
	public ActionBar.Tab findTabByTag(@Nonnull String tag) {
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			for (int i = 0; i < actionBar.getTabCount(); i++) {
				final ActionBar.Tab tab = actionBar.getTabAt(i);
				if (tab != null && tag.equals(tab.getTag())) {
					return tab;
				}
			}
		}

		return null;
	}

	private class MessengerEventListener extends AbstractJEventListener<MessengerEvent> {

		protected MessengerEventListener() {
			super(MessengerEvent.class);
		}

		@Override
		public void onEvent(@Nonnull MessengerEvent event) {
			switch (event.getType()) {
				case unread_messages_count_changed:
					invalidateOptionsMenu();
					break;
				case notification_removed:
				case notification_added:
					invalidateOptionsMenu();
					break;
			}
		}
	}
}
