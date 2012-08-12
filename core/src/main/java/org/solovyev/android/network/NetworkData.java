package org.solovyev.android.network;

import android.net.NetworkInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 7/26/12
 * Time: 5:26 PM
 */
public interface NetworkData {

    @NotNull
    NetworkState getState();

    /**
     * An optional reason for the connectivity state change may have been supplied.
     *
     * @return the reason for the state change, if available, or {@code null}
     *         otherwise.
     */
    @Nullable
    String getReason();

    /**
     * Returns true if the most recent event was for an attempt to switch over to
     * a new network following loss of connectivity on another network.
     *
     * @return {@code true} if this was a failover attempt, {@code false} otherwise.
     */
    boolean isFailover();

    /**
     * Network connectivity information
     */
    @Nullable
    NetworkInfo getNetworkInfo();

    /**
     * In case of a Disconnect, the connectivity manager may have
     * already established, or may be attempting to establish, connectivity
     * with another network. If so, {@code mOtherNetworkInfo} will be non-null.
     */
    @Nullable
    NetworkInfo getOtherNetworkInfo();
}
