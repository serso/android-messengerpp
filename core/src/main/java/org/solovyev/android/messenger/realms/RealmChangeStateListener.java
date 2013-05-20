package org.solovyev.android.messenger.realms;

import android.app.Activity;
import com.google.common.util.concurrent.FutureCallback;
import org.solovyev.android.messenger.MessengerContextCallback;
import org.solovyev.android.tasks.Tasks;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:33 PM
 */
final class RealmChangeStateListener extends MessengerContextCallback<Activity, Realm> {

	private RealmChangeStateListener() {
	}

	@Nonnull
	static FutureCallback<Realm> newInstance(@Nonnull Activity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new RealmChangeStateListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity context, Realm realm) {
		final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
		eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(realm, RealmGuiEventType.FinishedState.status_changed));
	}
}
