package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import com.google.inject.Inject;
import org.solovyev.android.messenger.chats.ChatGuiEvent;
import org.solovyev.android.messenger.chats.ChatGuiEventListener;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.FragmentGuiEvent;
import org.solovyev.android.messenger.preferences.MessengerOnPreferenceAttachedListener;
import org.solovyev.android.messenger.preferences.PreferenceGuiEvent;
import org.solovyev.android.messenger.preferences.PreferenceGuiEventListener;
import org.solovyev.android.messenger.preferences.PreferenceListFragment;
import org.solovyev.android.messenger.realms.RealmDefGuiEvent;
import org.solovyev.android.messenger.realms.RealmDefGuiEventListener;
import org.solovyev.android.messenger.realms.RealmGuiEvent;
import org.solovyev.android.messenger.realms.RealmGuiEventListener;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.ContactGuiEvent;
import org.solovyev.android.messenger.users.ContactGuiEventListener;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nullable
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
		listeners.add(GuiEvent.class, new GuiEventListener(this));
		listeners.add(RealmGuiEvent.class, new RealmGuiEventListener(this));
		listeners.add(RealmDefGuiEvent.class, new RealmDefGuiEventListener(this));
		listeners.add(ContactGuiEvent.class, new ContactGuiEventListener(this, getAccountService()));
		listeners.add(ChatGuiEvent.class, new ChatGuiEventListener(this, getChatService()));
		listeners.add(FragmentGuiEvent.class, new FragmentGuiEventListener(this));
		listeners.add(PreferenceGuiEvent.class, new PreferenceGuiEventListener(this));

		if (isDualPane()) {
			getMultiPaneFragmentManager().emptifySecondFragment();
		}

		if (isTriplePane()) {
			getMultiPaneFragmentManager().emptifyThirdFragment();
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

	private static final class FragmentGuiEventListener implements EventListener<FragmentGuiEvent> {

		@Nonnull
		private final MessengerFragmentActivity activity;

		private FragmentGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
			this.activity = activity;
		}

		@Override
		public void onEvent(@Nonnull FragmentGuiEvent event) {
			switch (event.getType()) {
				case created:
					break;
				case shown:
					break;
				case started:
                    /*if (event.getParentViewId() == R.id.content_first_pane) {
                        // if new fragment is shown on the first pane => emptify other panes
                        if (activity.isDualPane()) {
                            activity.emptifySecondFragment();
                            if ( activity.isTriplePane() ) {
                                activity.emptifyThirdFragment();
                            }
                        }
                    }*/
					break;
			}
		}
	}
}
