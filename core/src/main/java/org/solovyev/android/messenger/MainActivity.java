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
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessagesFragment;
import org.solovyev.android.messenger.users.CompositeUserDialogFragment;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.SwipeGestureListener;
import org.solovyev.android.wizard.Wizard;
import org.solovyev.android.wizard.Wizards;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.RoboGuice;
import roboguice.event.EventListener;
import roboguice.event.EventManager;
import roboguice.event.Observes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.MessengerPreferences.isNewInstallation;
import static org.solovyev.android.messenger.UiThreadEventListener.onUiThread;
import static org.solovyev.android.messenger.accounts.Accounts.withAccountException;
import static org.solovyev.android.messenger.chats.Chats.CHATS_FRAGMENT_TAG;
import static org.solovyev.android.messenger.chats.Chats.openUnreadChat;
import static org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager.tabFragments;
import static org.solovyev.android.messenger.messages.MessagesFragment.newMessagesFragmentDef;
import static org.solovyev.android.messenger.users.ContactFragment.newViewContactFragmentDef;
import static org.solovyev.android.messenger.users.ContactUiEventType.call;
import static org.solovyev.android.messenger.users.ContactsInfoFragment.newViewContactsFragmentDef;
import static org.solovyev.android.messenger.users.Users.CONTACTS_FRAGMENT_TAG;
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
	private static final String INTENT_OPEN_CHAT_ACTION = "open_chat";
	private static final String INTENT_OPEN_CHAT_ACTION_CHAT_ID = "chat_id";

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private boolean tabsEnabled;

	@Nullable
	private ActivityMenu<Menu, MenuItem> menu;

	@Nullable
	private GestureDetector gestureDetector;

	@Nonnull
	private final JEventListener<MessengerEvent> messengerEventListener = onUiThread(this, new MessengerEventListener());

    /*
	**********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public static boolean tryStart(@Nonnull Activity activity) {
		// todo serso: currently if app is opened via Ongoing Notification activity stack is cleared and and MainActivity is shown
		// instead of last opened activity (e.g. AccountsActivity)
		final boolean shouldStart = true;

		if (shouldStart) {
			final Intent result = new Intent();
			result.setClass(activity, MainActivity.class);
			activity.startActivity(result);
		}

		return shouldStart;
	}

	public static void startForUnreadMessages(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, MainActivity.class);
		result.setAction(INTENT_SHOW_UNREAD_MESSAGES_ACTION);
		activity.startActivity(result);
	}

	public static void startForChat(@Nonnull Activity activity, @Nonnull Chat chat) {
		final Intent result = new Intent();
		result.setClass(activity, MainActivity.class);
		result.setAction(INTENT_OPEN_CHAT_ACTION);
		result.putExtra(INTENT_OPEN_CHAT_ACTION_CHAT_ID, chat.getEntity());
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

		ChatUiEventListener.listenTo(this);

		initTabs(savedInstanceState);

		initFragments();

		handleIntent(getIntent());
	}

	private void handleIntent(@Nonnull Intent intent) {
		final String action = intent.getAction();
		if (areEqual(action, INTENT_SHOW_UNREAD_MESSAGES_ACTION)) {
			getUiHandler().post(new Runnable() {
				@Override
				public void run() {
					openUnreadChat(MainActivity.this);
				}
			});
		} else if (areEqual(action, INTENT_OPEN_CHAT_ACTION)) {
			final Parcelable chatId = intent.getParcelableExtra(INTENT_OPEN_CHAT_ACTION_CHAT_ID);
			if (chatId instanceof Entity) {
				getUiHandler().post(new Runnable() {
					@Override
					public void run() {
						Chats.openChat(MainActivity.this, (Entity) chatId);
					}
				});
			}
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

		this.getMessengerListeners().addListener(messengerEventListener);

		final RoboListeners listeners = getListeners();

		listeners.add(ContactUiEvent.Typed.class, new ContactUiEventListener(this, getAccountService()));
		listeners.add(ContactUiEvent.ShowCompositeDialog.class, new EventListener<ContactUiEvent.ShowCompositeDialog>() {
			@Override
			public void onEvent(ContactUiEvent.ShowCompositeDialog event) {
				CompositeUserDialogFragment.show(event.contact, event.nextEventType, MainActivity.this);
			}
		});

		if (isNewInstallation()) {
			final Wizards wizards = getWizards();
			final Wizard wizard = wizards.getWizard(FIRST_TIME_WIZARD);
			if (!wizard.isFinished()) {
				continueWizard(wizards, wizard.getName(), this);
			}
		}

		if (isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof ChatsFragment) {
				final ChatListItem item = ((ChatsFragment) fragment).getAdapter().getSelectedItem();
				if (item != null) {
					getUiHandler().post(new Runnable() {
						@Override
						public void run() {
							getEventManager().fire(new ChatUiEvent.Clicked(item.getChat()));
						}
					});
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
		this.getMessengerListeners().removeListener(messengerEventListener);

		super.onPause();
	}

	private void initTabs(@Nullable Bundle savedInstanceState) {
		final ActionBar actionBar = getSupportActionBar();

		final boolean showTabs = tabFragments.size() > 1;
		if (showTabs) {
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

			if (selectedTab == -1) {
				// activity created first time => we must select first tab
				actionBar.setSelectedNavigationItem(0);
			}
		} else {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			if (savedInstanceState == null) {
				// activity created first time => we must set main fragment
				fragmentManager.setMainFragment(tabFragments.get(0));
			}
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
		if (this.menu == null) {
			this.menu = new MainMenu(new HomeButtonListener());
		}
		return this.menu.onCreateOptionsMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this, item);
	}

	@Override
	public void onBackStackChanged() {
		if (!isDialog()) {
			final ActionBar actionBar = getSupportActionBar();
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			} else {
				actionBar.setDisplayHomeAsUpEnabled(false);
			}
		}

		tryUpdateActionBar();
	}

	private void tryUpdateActionBar() {
		final Fragment firstFragment = getMultiPaneFragmentManager().getFirstFragment();
		if (firstFragment instanceof BaseListFragment) {
			((BaseListFragment) firstFragment).tryUpdateActionBar();
		} else {
			invalidateOptionsMenu();
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

	@Override
	protected void onContactRemoved(@Nonnull String contactId) {
		if (!isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof MessagesFragment) {
				final MessagesFragment mf = (MessagesFragment) fragment;
				final Chat chat = mf.getChat();

				if (chat.isPrivate()) {
					if (chat.getSecondUser().getEntityId().equals(contactId)) {
						tryGoBack();
					}
				}
			}
		}
	}

	@Override
	protected void onChatRemoved(@Nonnull String chatId) {
		if (!isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof MessagesFragment) {
				final MessagesFragment mf = (MessagesFragment) fragment;
				final Chat chat = mf.getChat();

				if (chat.getId().equals(chatId)) {
					tryGoBack();
				}
			}
		}
	}

	@Override
	protected void onAccountDisabled(@Nonnull Account account) {
		if (!isDualPane()) {
			final Fragment fragment = fragmentManager.getFirstFragment();
			if (fragment instanceof MessagesFragment) {
				final MessagesFragment mf = (MessagesFragment) fragment;

				final Chat chat = mf.getChat();
				final Account chatAccount = getAccountService().getAccountByEntity(chat.getEntity());
				if (account.equals(chatAccount)) {
					tryGoBack();
				}
			}
		}
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

	private static final class ContactUiEventListener implements EventListener<ContactUiEvent.Typed> {

		@Nonnull
		private final BaseFragmentActivity activity;

		@Nonnull
		private final AccountService accountService;

		private ContactUiEventListener(@Nonnull BaseFragmentActivity activity, @Nonnull AccountService accountService) {
			this.activity = activity;
			this.accountService = accountService;
		}

		@Override
		public void onEvent(@Nonnull ContactUiEvent.Typed event) {
			final User contact = event.contact;

			final Account account = accountService.getAccountByEntity(contact.getEntity());
			switch (event.type) {
				case call:
					if (account.isCompositeUser(contact)) {
						if (!account.isCompositeUserDefined(contact)) {
							fireEvent(new ContactUiEvent.ShowCompositeDialog(contact, call));
						} else {
							onCallContactChat(contact);
						}
					} else {
						onCallContactChat(contact);
					}
					break;
				case mark_all_messages_read:
					onMarkAllMessagesRead(contact);
					break;
			}
		}

		private void onCallContactChat(@Nonnull User contact) throws UnsupportedAccountException {
			final Account account = App.getAccountService().getAccountByEntity(contact.getEntity());
			if (account.canCall(contact)) {
				account.call(contact, activity);
			}
		}

		private void onMarkAllMessagesRead(@Nonnull User contact) throws UnsupportedAccountException {
			final Account account = App.getAccountService().getAccountByEntity(contact.getEntity());
			final Chat chat = App.getChatService().getPrivateChat(account.getUser().getEntity(), contact.getEntity());
			if (chat != null) {
				final EventManager eventManager = App.getEventManager(activity);
				for (Message message : getMessageService().getMessages(chat.getEntity())) {
					if (message.canRead()) {
						eventManager.fire(new ChatUiEvent.MarkMessageRead(chat, message.cloneRead()));
					}
				}
			}
		}

		private void fireEvent(@Nonnull ContactUiEvent event) {
			App.getEventManager(activity).fire(event);
		}
	}

	private static final class ChatUiEventListener {

		@Nonnull
		private final BaseFragmentActivity activity;

		@Inject
		@Nonnull
		private ChatService chatService;

		public ChatUiEventListener(@Nonnull BaseFragmentActivity activity) {
			this.activity = activity;
		}

		public void onShowParticipantsEvent(@Observes @Nonnull final ChatUiEvent.ShowParticipants event) {
			if (event.chat.isPrivate()) {
				final Entity contactId = chatService.getSecondUser(event.chat);
				if (contactId != null) {
					final User contact = App.getUserService().getUserById(contactId);
					App.getEventManager(activity).fire(new ContactUiEvent.Open(contact));
				}
			} else {
				ChatParticipantsActivity.open(activity, event.chat);
			}
		}

		public void onOpenChatEvent(@Observes @Nonnull final ChatUiEvent.Open event) {
			final Chat chat = event.chat;
			final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

			fm.clearBackStack();
			final boolean fragmentSet;

			final BaseListFragment<?> chatsFragment = fm.getFragment(CHATS_FRAGMENT_TAG);
			if (chatsFragment != null && chatsFragment.isVisible()) {
				fragmentSet = chatsFragment.clickItemById(chat.getId());
				if (!fragmentSet) {
					chatsFragment.unselect();
				}
			} else {
				if (chat.isPrivate()) {
					final Entity contact = chat.getSecondUser();
					final BaseListFragment<?> contactsFragment = fm.getFragment(CONTACTS_FRAGMENT_TAG);
					if (contactsFragment != null && contactsFragment.isVisible()) {
						fragmentSet = contactsFragment.clickItemById(contact.getEntityId());
						if (!fragmentSet) {
							contactsFragment.unselect();
						}
					} else {
						fragmentSet = false;
					}
				} else {
					fragmentSet = false;
				}
			}

			if (!fragmentSet) {
				if (activity.isDualPane()) {
					fm.setSecondFragment(newMessagesFragmentDef(activity, chat, false));
				}
			}

			if (!activity.isDualPane()) {
				fm.setMainFragment(newMessagesFragmentDef(activity, chat, true));
			}

			updateThirdPaneForNewChat(chat, fm);
		}

		public void onMessageReadEvent(@Observes @Nonnull final ChatUiEvent.MarkMessageRead event) {
			executeInBackground(withAccountException(new AccountRunnable() {
				@Override
				public void run() throws AccountConnectionException {
					chatService.markMessageRead(event.chat, event.message);
				}
			}));
		}

		public void onChatClickedEvent(@Observes @Nonnull final ChatUiEvent.Clicked event) {
			final MessengerMultiPaneFragmentManager fm = activity.getMultiPaneFragmentManager();

			fm.clearBackStack();
			if (activity.isDualPane()) {
				fm.setSecondFragment(newMessagesFragmentDef(activity, event.chat, false));
				updateThirdPaneForNewChat(event.chat, fm);
			} else {
				fm.setMainFragment(newMessagesFragmentDef(activity, event.chat, true));
			}
		}

		private void updateThirdPaneForNewChat(@Nonnull Chat chat, @Nonnull MessengerMultiPaneFragmentManager fm) {
			if (activity.isTriplePane()) {
				final Account account = activity.getAccountService().getAccountByEntity(chat.getEntity());

				if (chat.isPrivate()) {
					fm.setThirdFragment(newViewContactFragmentDef(activity, account, chat.getSecondUser(), false));
				} else {
					final List<User> participants = activity.getChatService().getParticipantsExcept(chat.getEntity(), account.getUser().getEntity());
					fm.setThirdFragment(newViewContactsFragmentDef(activity, participants, false));
				}
			}
		}

		public static void listenTo(@Nonnull BaseFragmentActivity activity) {
			final ChatUiEventListener listener = new ChatUiEventListener(activity);
			RoboGuice.getInjector(activity).injectMembers(listener);
		}
	}
}
