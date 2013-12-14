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

package org.solovyev.android.messenger.accounts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.connection.AccountConnectionsService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.notifications.Notifications;
import org.solovyev.android.network.NetworkData;
import org.solovyev.android.network.NetworkStateListener;
import org.solovyev.android.network.NetworkStateService;
import org.solovyev.android.sherlock.AndroidSherlockUtils;
import roboguice.RoboGuice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoInternetConnectionDialog extends DialogFragment {

	@Inject
	@Nonnull
	private NetworkStateService networkStateService;

	@Inject
	@Nonnull
	private AccountConnectionsService accountConnectionsService;

	@Nonnull
	public static final String TAG = NoInternetConnectionDialog.class.getSimpleName();

	@Nullable
	private NetworkStateListener networkListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b.setMessage(R.string.mpp_notification_network_problem);
		b.setPositiveButton(R.string.mpp_turn_on_internet, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				Notifications.NO_INTERNET_NOTIFICATION.solveOnClick();
			}
		});
		b.setCancelable(false);
		return b.create();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (accountConnectionsService.isInternetConnectionExists()) {
			dismiss();
		} else {
			networkListener = new DialogNetworkStateListener();
			networkStateService.addListener(networkListener);
		}
	}

	@Override
	public void onPause() {
		if (networkListener != null) {
			networkStateService.removeListener(networkListener);
			networkListener = null;
		}

		super.onPause();
	}

	public static void show(@Nonnull FragmentActivity activity) {
		AndroidSherlockUtils.showDialog(new NoInternetConnectionDialog(), TAG, activity.getSupportFragmentManager());
	}

	private class DialogNetworkStateListener implements NetworkStateListener {

		@Override
		public void onNetworkEvent(@Nonnull NetworkData networkData) {
			switch (networkData.getState()) {
				case CONNECTED:
					dismiss();
					break;
			}
		}
	}
}