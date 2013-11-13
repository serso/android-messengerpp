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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.PrimaryFragment;
import org.solovyev.android.messenger.messages.EmptyFragment;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Stack;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.App.newTag;

public abstract class BaseFragmentActivity extends RoboSherlockFragmentActivity {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */


	protected final String TAG = newTag(this.getClass().getSimpleName());

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
	private MultiPaneManager multiPaneManager;

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

	@Nullable
	private ViewGroup secondPane;

	@Nullable
	private ViewGroup thirdPane;

	@Nonnull
	private final MessengerMultiPaneFragmentManager multiPaneFragmentManager;

	private RoboListeners listeners;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	protected BaseFragmentActivity(int layoutId) {
		this.layoutId = layoutId;
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
	public MultiPaneManager getMultiPaneManager() {
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

	@Nonnull
	public MessengerListeners getMessengerListeners() {
		return messengerListeners;
	}

	public RoboListeners getListeners() {
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(layoutId);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		this.secondPane = (ViewGroup) findViewById(R.id.content_second_pane);
		this.thirdPane = (ViewGroup) findViewById(R.id.content_third_pane);

		listeners = new RoboListeners(getEventManager());

	}

	protected void initFragments() {
		if (isDualPane()) {
			initDualPaneFragments();
		} else {
			initMainPaneFragment();
		}
	}

	private void initMainPaneFragment() {
		final FragmentManager fm = getSupportFragmentManager();
		final MessengerMultiPaneFragmentManager multiPaneFragmentManager = getMultiPaneFragmentManager();

		final Fragment mainFragment = fm.findFragmentById(R.id.content_first_pane);
		if (mainFragment != null) {
			final PrimaryFragment primaryFragment = getPrimaryFragment(mainFragment);
			if (primaryFragment != null) {

				final Stack<MultiPaneFragmentDef> fragmentDefs = new Stack<MultiPaneFragmentDef>();

				while (true) {
					final Fragment secondFragment = fm.findFragmentById(R.id.content_second_pane);
					if (secondFragment != null) {
						// fragment should be copied before popping back stack
						final MultiPaneFragmentDef fragmentDef = newCopyingFragmentDef(secondFragment, true);
						if (!primaryFragment.isAddToBackStack()) {
							if (fm.popBackStackImmediate()) {
								tryPushSecondFragment(fragmentDefs, fragmentDef, secondFragment);
							} else {
								tryPushSecondFragment(fragmentDefs, fragmentDef, secondFragment);
								// nothing to pop => stop
								break;
							}
						} else {
							// primary fragment itself is on back stack => we cannot pop back stack as we can pop it.
							// let's just add latest fragment on the second pane and put it on on the stack
							tryPushSecondFragment(fragmentDefs, fragmentDef, secondFragment);
							break;
						}
					} else {
						// no second fragment => stop
						break;
					}
				}

				while (!fragmentDefs.isEmpty()) {
					multiPaneFragmentManager.setMainFragment(fragmentDefs.pop());
				}
			}
		}
	}

	private void tryPushSecondFragment(@Nonnull Stack<MultiPaneFragmentDef> fragmentDefs, @Nonnull MultiPaneFragmentDef secondFragmentCopy, @Nonnull Fragment secondFragment) {
		if (!(secondFragment instanceof EmptyFragment)) {
			fragmentDefs.push(secondFragmentCopy);
		}
	}

	private void initDualPaneFragments() {
		final FragmentManager fm = getSupportFragmentManager();

		// We need to be sure that the fragment which is shown on the main pane is primary.
		// If it's not a primary fragment we need to move it to the secondary pane.
		// To restore the primary fragment we just pop back stack (as primary fragment must be somewhere in back stack)
		// As we need to restore second fragment's state let's copy arguments and instance state and pass it to the newly created argument.

		final Stack<MultiPaneFragmentDef> fragmentDefs = new Stack<MultiPaneFragmentDef>();

		while (true) {
			final Fragment mainFragment = fm.findFragmentById(R.id.content_first_pane);
			if (mainFragment != null) {
				if(!isPrimaryFragment(mainFragment)) {
					// NOTE: we must save local copies before popping the back stack as these values might change
					final MultiPaneFragmentDef fragmentDef = newCopyingFragmentDef(mainFragment, false);

					if (fm.popBackStackImmediate()) {
						fragmentDefs.push(fragmentDef);
					} else {
						// nothing to pop => stop
						if(fragmentDefs.isEmpty()) {
							final ActionBar.Tab selectedTab = getSupportActionBar().getSelectedTab();
							if (selectedTab != null) {
								selectedTab.select();
							}
						}
						break;
					}
				} else {
					// primary fragment => stop
					break;
				}
			} else {
				// main pane empty => stop
				break;
			}
		}

		boolean first = true;
		while (!fragmentDefs.isEmpty()) {
			MultiPaneFragmentDef fragmentDef = fragmentDefs.pop();
			if (first) {
				first = false;
				fragmentDef = copy(fragmentDef, false);
			} else {
				fragmentDef = copy(fragmentDef, true);
			}

			getMultiPaneFragmentManager().setSecondFragment(fragmentDef);
		}
	}

	@Nonnull
	private MultiPaneFragmentDef newCopyingFragmentDef(@Nonnull final Fragment fragment, boolean addToBackStack) {
		final FragmentManager fm = getSupportFragmentManager();

		final Fragment.SavedState fragmentSavedState = fm.saveFragmentInstanceState(fragment);
		final String fragmentTag = fragment.getTag();
		final Bundle fragmentArguments = fragment.getArguments();

		return MultiPaneFragmentDef.newInstance(fragmentTag, addToBackStack, new Builder<Fragment>() {
			@Nonnull
			@Override
			public Fragment build() {
				final Fragment newFragment = Fragment.instantiate(BaseFragmentActivity.this, fragment.getClass().getName(), fragmentArguments);
				newFragment.setInitialSavedState(fragmentSavedState);
				return newFragment;
			}
		}, null);
	}

	@Nonnull
	private MultiPaneFragmentDef copy(@Nonnull final MultiPaneFragmentDef fragmentDef, boolean addToBackStack) {
		return MultiPaneFragmentDef.newInstance(fragmentDef.getTag(), addToBackStack, new Builder<Fragment>() {
					@Nonnull
					@Override
					public Fragment build() {
						return fragmentDef.build();
					}
				}, new JPredicate<Fragment>() {
					@Override
					public boolean apply(Fragment fragment) {
						return fragmentDef.canReuse(fragment);
					}
				}
		);
	}


	private boolean isPrimaryFragment(@Nonnull final Fragment fragment) {
		return any(asList(PrimaryFragment.values()), new Predicate<PrimaryFragment>() {
			@Override
			public boolean apply(PrimaryFragment pf) {
				return pf.getFragmentTag().equals(fragment.getTag());
			}
		});
	}

	@Nullable
	private PrimaryFragment getPrimaryFragment(@Nonnull final Fragment fragment) {
		return find(asList(PrimaryFragment.values()), new Predicate<PrimaryFragment>() {
			@Override
			public boolean apply(PrimaryFragment pf) {
				return pf.getFragmentTag().equals(fragment.getTag());
			}
		}, null);
	}

	@Override
	protected void onDestroy() {
		if (listeners != null) {
			listeners.clearAll();
		}

		super.onDestroy();
	}
}
