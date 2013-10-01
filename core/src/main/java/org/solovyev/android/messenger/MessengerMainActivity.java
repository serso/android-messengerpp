package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.AccountUiEventListener;
import org.solovyev.android.messenger.chats.ChatUiEvent;
import org.solovyev.android.messenger.chats.ChatUiEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.FragmentUiEvent;
import org.solovyev.android.messenger.preferences.MessengerOnPreferenceAttachedListener;
import org.solovyev.android.messenger.preferences.PreferenceListFragment;
import org.solovyev.android.messenger.preferences.PreferenceUiEvent;
import org.solovyev.android.messenger.preferences.PreferenceUiEventListener;
import org.solovyev.android.messenger.realms.RealmUiEvent;
import org.solovyev.android.messenger.realms.RealmUiEventListener;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.ContactUiEventListener;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:52 PM
 */
public final class MessengerMainActivity extends MessengerFragmentActivity implements PreferenceListFragment.OnPreferenceAttachedListener {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private SyncService syncService;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private RoboListeners listeners;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public MessengerMainActivity() {
		super(R.layout.mpp_main);
	}

	public static void startActivity(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, MessengerMainActivity.class);
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

		listeners = new RoboListeners(getEventManager());
		listeners.add(UiEvent.class, new UiEventListener(this));
		listeners.add(AccountUiEvent.class, new AccountUiEventListener(this));
		listeners.add(RealmUiEvent.class, new RealmUiEventListener(this));
		listeners.add(ContactUiEvent.class, new ContactUiEventListener(this, getAccountService()));
		listeners.add(ChatUiEvent.class, new ChatUiEventListener(this, getChatService()));
		listeners.add(FragmentUiEvent.class, new FragmentUiEventListener(this));
		listeners.add(PreferenceUiEvent.class, new PreferenceUiEventListener(this));

		if (isDualPane()) {
			getMultiPaneFragmentManager().emptifySecondFragment();
		} else {
			removeFragmentByViewId(R.id.content_second_pane);
		}

		if (isTriplePane()) {
			getMultiPaneFragmentManager().emptifyThirdFragment();
		} else {
			removeFragmentByViewId(R.id.content_third_pane);
		}
	}

	private void removeFragmentByViewId(int viewId) {
		final FragmentManager fm = getSupportFragmentManager();
		final Fragment fragmentById = fm.findFragmentById(viewId);
		if (fragmentById != null) {
			final FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fragmentById);
			ft.commitAllowingStateLoss();
			fm.executePendingTransactions();
		}
	}


	@Override
	protected void onDestroy() {
		if (listeners != null) {
			listeners.clearAll();
		}

		super.onDestroy();
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId) {
		new MessengerOnPreferenceAttachedListener(this, syncService).onPreferenceAttached(preferenceScreen, preferenceResId);
	}

	public RoboListeners getListeners() {
		return listeners;
	}

	private static final class FragmentUiEventListener implements EventListener<FragmentUiEvent> {

		@Nonnull
		private final MessengerFragmentActivity activity;

		private FragmentUiEventListener(@Nonnull MessengerFragmentActivity activity) {
			this.activity = activity;
		}

		@Override
		public void onEvent(@Nonnull FragmentUiEvent event) {
			switch (event.getType()) {
				case created:
					break;
				case shown:
					break;
				case started:
/*					if (event.getParentViewId() == R.id.content_first_pane) {
						// if new fragment is shown on the first pane => emptify other panes
						if (activity.isDualPane()) {
							activity.getMultiPaneFragmentManager().emptifySecondFragment();
							if (activity.isTriplePane()) {
								activity.getMultiPaneFragmentManager().emptifyThirdFragment();
							}
						}
					}*/
					break;
			}
		}
	}
}
