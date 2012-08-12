package org.solovyev.android.network;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 7/26/12
 * Time: 5:26 PM
 */
public interface NetworkStateListener {

    void onNetworkEvent(@NotNull NetworkData networkData);
}
