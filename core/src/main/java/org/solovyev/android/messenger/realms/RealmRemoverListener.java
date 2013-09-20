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
 * Time: 1:44 PM
 */
final class RealmRemoverListener extends MessengerContextCallback<Activity, Account> {

	private RealmRemoverListener() {
	}

	@Nonnull
	static FutureCallback<Account> newInstance(@Nonnull Activity activity) {
		return Tasks.toUiThreadFutureCallback(activity, new RealmRemoverListener());
	}

	@Override
	public void onSuccess(@Nonnull Activity activity, Account account) {
		final EventManager eventManager = RoboGuice.getInjector(activity).getInstance(EventManager.class);
		eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(account, RealmGuiEventType.FinishedState.removed));
	}
}
