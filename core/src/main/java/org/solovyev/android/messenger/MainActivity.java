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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.AccountUiEventListener;
import org.solovyev.android.messenger.chats.ChatUiEvent;
import org.solovyev.android.messenger.chats.ChatUiEventListener;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.ContactUiEventListener;
import org.solovyev.android.view.SwipeGestureListener;
import org.solovyev.android.wizard.Wizard;
import org.solovyev.android.wizard.Wizards;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.MessengerPreferences.isNewInstallation;
import static org.solovyev.android.messenger.UiThreadEventListener.onUiThread;
import static org.solovyev.android.messenger.chats.Chats.openUnreadChat;
import static org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager.tabFragments;
import static org.solovyev.android.messenger.wizard.MessengerWizards.FIRST_TIME_WIZARD;
import static org.solovyev.android.wizard.WizardUi.continueWizard;
import static org.solovyev.common.Objects.areEqual;

public final class MainActivity extends BaseFragmentActivity {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final String SELECTED_TAB = "selected_tab";

	private static final String INTENT_SHOW_UNREAD_MESSAGES_ACTION = "show_unread_messages";

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private boolean tabsEnabled = false;

	@Nonnull
	private ActivityMenu<Menu, MenuItem> menu;

	@Nullable
	private GestureDetector gestureDetector;

	@Nullable
	private JEventListener<MessengerEvent> messengerEventListener;

    /*
	**********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public static void start(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, MainActivity.class);
		activity.startActivity(result);
	}

	public static void startForUnreadMessages(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, MainActivity.class);
		result.setAction(INTENT_SHOW_UNREAD_MESSAGES_ACTION);
		activity.startActivity(result);
	}

    /*
    **********************************************************************
    *
    *                           ACTIVITY LIFECYCLE METHODS
    *
    **********************************************************************
    */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// menu must be initialized before fragments as some fragments might add entries to menu
		this.menu = new MainMenu(new HomeButtonListener());

		initTabs(savedInstanceState);

		initFragments();

		handleIntent(getIntent());
	}

	private void handleIntent(@Nonnull Intent intent) {
		if (areEqual(intent.getAction(), INTENT_SHOW_UNREAD_MESSAGES_ACTION)) {
			getUiHandler().post(new Runnable() {
				@Override
				public void run() {
					openUnreadChat(MainActivity.this);
				}
			});
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final boolean handled = gestureDetector != null && gestureDetector.onTouchEvent(event);
		return handled || super.dispatchTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.messengerEventListener = onUiThread(this, new MessengerEventListener());
		this.getMessengerListeners().addListener(messengerEventListener);

		final RoboListeners listeners = getListeners();

		listeners.add(UiEvent.class, new UiEventListener(this));
		listeners.add(AccountUiEvent.class, new AccountUiEventListener(this));
		listeners.add(ContactUiEvent.class, new ContactUiEventListener(this, getAccountService()));
		listeners.add(ChatUiEvent.class, new ChatUiEventListener(this, getChatService()));

		if (!isMonkeyRunner()) {
			if (isNewInstallation()) {
				final Wizards wizards = getWizards();
				final Wizard wizard = wizards.getWizard(FIRST_TIME_WIZARD);
				if (!wizard.isFinished()) {
					continueWizard(wizards, wizard.getName(), this);
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(SELECTED_TAB, getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	protected void onPause() {
		if (this.messengerEventListener != null) {
			this.getMessengerListeners().removeListener(messengerEventListener);
			this.messengerEventListener = null;
		}

		super.onPause();
	}

	private void initTabs(@Nullable Bundle savedInstanceState) {
		final ActionBar actionBar = getSupportActionBar();

		tabsEnabled = false;
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		for (PrimaryFragment tabFragment : tabFragments) {
			addTab(tabFragment);
		}

		int selectedTab = -1;
		if (savedInstanceState != null) {
			selectedTab = savedInstanceState.getInt(SELECTED_TAB, -1);
		}

		if (selectedTab >= 0) {
			actionBar.setSelectedNavigationItem(selectedTab);
		}

		gestureDetector = new GestureDetector(this, new SwipeTabsGestureListener());

		tabsEnabled = true;

		// activity created first time => we must select first tab
		if (selectedTab == -1) {
			actionBar.setSelectedNavigationItem(0);
		}
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

	@Override
	public void onBackStackChanged() {
		final ActionBar actionBar = getSupportActionBar();
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
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


	private void addTab(@Nonnull final PrimaryFragment primaryFragment) {
		final String fragmentTag = primaryFragment.getFragmentTag();

		final ActionBar actionBar = getSupportActionBar();
		final ActionBar.Tab tab = actionBar.newTab();
		tab.setTag(fragmentTag);
		tab.setText(primaryFragment.getTitleResId());
		tab.setTabListener(new ActionBar.TabListener() {
			@Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				if (tabsEnabled) {
					final MessengerMultiPaneFragmentManager mpfm = getMultiPaneFragmentManager();
					mpfm.clearBackStack();
					mpfm.setMainFragment(primaryFragment, getSupportFragmentManager(), ft);
				}
			}

			@Override
			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
				if (tabsEnabled) {
					final MessengerMultiPaneFragmentManager mpfm = getMultiPaneFragmentManager();
					mpfm.clearBackStack();
					// in some cases we reuse pane for another fragment under same tab -> we need to reset fragment (in case if fragment has not been changed nothing is done)
					mpfm.setMainFragment(primaryFragment, getSupportFragmentManager(), ft);
				}
			}
		});
		actionBar.addTab(tab);
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

	private class SwipeTabsGestureListener extends SwipeGestureListener {

		public SwipeTabsGestureListener() {
			super(MainActivity.this);
		}

		@Override
		protected void onSwipeToRight() {
			changeTab(false);
		}

		@Override
		protected void onSwipeToLeft() {
			changeTab(true);
		}
	}

	private class HomeButtonListener implements Runnable {
		@Override
		public void run() {
			if (!getMultiPaneFragmentManager().goBackImmediately()) {
				final ActionBar.Tab tab = findTabByTag(PrimaryFragment.contacts.getFragmentTag());
				if (tab != null) {
					tab.select();
				}
			}
		}
	}
}
