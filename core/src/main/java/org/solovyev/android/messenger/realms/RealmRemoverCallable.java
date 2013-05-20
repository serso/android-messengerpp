package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerApplication;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:02 PM
 */
final class RealmRemoverCallable implements Callable<Realm> {

	@Nonnull
	static final String TASK_NAME = "realm-remove";

	@Nonnull
	private final Realm realm;

	RealmRemoverCallable(@Nonnull Realm realm) {
		this.realm = realm;
	}

	@Override
	public Realm call() {
		MessengerApplication.getServiceLocator().getRealmService().removeRealm(realm.getId());

		return realm;
	}
}
