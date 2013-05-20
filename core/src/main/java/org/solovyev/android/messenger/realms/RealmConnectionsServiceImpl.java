package org.solovyev.android.messenger.realms;

import android.app.Application;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.MessengerListeners;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkState;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.msg.MessageType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * User: serso
 * Date: 4/15/13
 * Time: 8:17 PM
 */
@Singleton
public final class RealmConnectionsServiceImpl implements RealmConnectionsService, NetworkStateListener {

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private RealmService realmService;

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private MessengerListeners messengerListeners;

	@Inject
	@Nonnull
	private NotificationService notificationService;

	@Nonnull
	private final Application context;

	/*
	**********************************************************************
	*
	*                           OWN FIELDS
	*
	**********************************************************************
	*/
	@Nonnull
	private RealmConnections realmConnections;

	@Nullable
	private RealmEventListener realmEventListener;

	@Inject
	public RealmConnectionsServiceImpl(@Nonnull Application context) {
		this.context = context;
	}

	@Override
	public void init() {
		realmConnections = new RealmConnections(context);

		networkStateService.addListener(this);

		realmEventListener = new RealmEventListener();
		realmService.addListener(realmEventListener);

		tryStartConnectionsFor(realmService.getEnabledRealms());
	}

	private void tryStartConnectionsFor(@Nonnull Collection<Realm> realms) {
		final boolean start = canStartConnection();
		realmConnections.startConnectionsFor(realms, start);
	}

	private boolean canStartConnection() {
		final NetworkData networkData = networkStateService.getNetworkData();
		return networkData.getState() == NetworkState.CONNECTED;
	}

	@Override
	public void onNetworkEvent(@Nonnull NetworkData networkData) {
		switch (networkData.getState()) {
			case UNKNOWN:
				break;
			case CONNECTED:
				notificationService.removeNotification(R.string.mpp_notification_network_problem);
				notificationService.removeNotification(R.string.mpp_notification_realm_connection_exception);
				realmConnections.tryStartAll();
				break;
			case NOT_CONNECTED:
				notificationService.addNotification(R.string.mpp_notification_network_problem, MessageType.warning);
				realmConnections.tryStopAll();
				break;
		}
	}

	private final class RealmEventListener extends AbstractJEventListener<RealmEvent> implements JEventListener<RealmEvent> {

		private RealmEventListener() {
			super(RealmEvent.class);
		}

		@Override
		public void onEvent(@Nonnull RealmEvent event) {
			final Realm realm = event.getRealm();
			switch (event.getType()) {
				case created:
					tryStartConnectionsFor(Arrays.asList(realm));
					break;
				case changed:
					realmConnections.updateRealm(realm, canStartConnection());
					break;
				case state_changed:
					switch (realm.getState()) {
						case removed:
							realmConnections.removeConnectionFor(realm);
							break;
						default:
							if (realm.isEnabled()) {
								tryStartConnectionsFor(Arrays.asList(realm));
							} else {
								realmConnections.tryStopFor(realm);
							}
							break;
					}
					break;
				case stop:
					realmConnections.tryStopFor(realm);
					break;
				case start:
					tryStartConnectionsFor(Arrays.asList(realm));
					break;
			}
		}
	}
}
