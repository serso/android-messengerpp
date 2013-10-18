package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.AccountUiEvent;
import org.solovyev.android.messenger.accounts.AccountUiEventListener;
import org.solovyev.android.messenger.chats.ChatUiEvent;
import org.solovyev.android.messenger.chats.ChatUiEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.preferences.MessengerOnPreferenceAttachedListener;
import org.solovyev.android.messenger.preferences.PreferenceListFragment;
import org.solovyev.android.messenger.preferences.PreferenceUiEvent;
import org.solovyev.android.messenger.preferences.PreferenceUiEventListener;
import org.solovyev.android.messenger.realms.RealmUiEvent;
import org.solovyev.android.messenger.realms.RealmUiEventListener;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.ContactUiEventListener;

import javax.annotation.Nonnull;

public final class MainActivity extends BaseFragmentActivity implements PreferenceListFragment.OnPreferenceAttachedListener {

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

	public MainActivity() {
		super(R.layout.mpp_main);
	}

	public static void startActivity(@Nonnull Activity activity) {
		final Intent result = new Intent();
		result.setClass(activity, MainActivity.class);
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
		listeners.add(PreferenceUiEvent.class, new PreferenceUiEventListener(this));
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
}
