package org.solovyev.android.network;

/**
 * User: serso
 * Date: 7/26/12
 * Time: 5:15 PM
 */


/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NetworkStateController {

    private static final String TAG = "NetworkConnectivityController";
    private static final boolean DEBUG = false;

    @NotNull
    private static final NetworkStateController instance = new NetworkStateController();

    @Nullable
    private Context context;

    @NotNull
    private final List<NetworkStateListener> listeners = new ArrayList<NetworkStateListener>();

    @NotNull
    private NetworkData networkData;

    @NotNull
    private ConnectivityBroadcastReceiver receiver;

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(@NotNull Context context, @NotNull Intent intent) {
            final String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            final NetworkData localNetworkData = NetworkDataImpl.fromIntent(intent);

            networkData = localNetworkData;

            if (DEBUG) {
                Log.d(TAG, "onReceive(): " + localNetworkData);
            }

            // notify listeners


            final List<NetworkStateListener> localListeners;
            synchronized (listeners) {
                localListeners = new ArrayList<NetworkStateListener>(listeners);
            }

            for (NetworkStateListener localListener : localListeners) {
                localListener.onNetworkEvent(localNetworkData);
            }
        }
    }

    /**
     * Create a new NetworkConnectivityListener.
     */
    private NetworkStateController() {
        networkData = NetworkDataImpl.newUnknownNetworkData();
        receiver = new ConnectivityBroadcastReceiver();
    }

    /**
     * This method starts listening for network connectivity state changes.
     *
     * @param context must be application context
     */
    public synchronized void startListening(@NotNull Application context) {
        this.context = context;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
    }

    public boolean add(@NotNull NetworkStateListener listener) {
        return listeners.add(listener);
    }

    public boolean remove(@NotNull NetworkStateListener listener) {
        return listeners.remove(listener);
    }

    /**
     * This method stops this class from listening for network changes.
     */
    public synchronized void stopListening() {
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
        context = null;
        networkData = NetworkDataImpl.newUnknownNetworkData();
    }

    @NotNull
    public NetworkData getNetworkData() {
        return networkData;
    }

    @NotNull
    public static NetworkStateController getInstance() {
        return instance;
    }
}